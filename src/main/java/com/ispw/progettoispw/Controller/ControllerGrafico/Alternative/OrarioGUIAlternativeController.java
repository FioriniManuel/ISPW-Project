package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean; // <- VM
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrarioGUIAlternativeController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;

    // Riepilogo
    @FXML private Label clienteValue;
    @FXML private Label professionistaValue;
    @FXML private Label dataValue;
    @FXML private Label orarioValue;
    @FXML private Label servizioValue;
    @FXML private Label prezzoValue;

    @FXML private Button backButton;
    @FXML private Button payButton;

    private final BookingController booking = new BookingController();
    private BookingBean bean; // caricato dalla sessione

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        // Prendi la bean dalla "sessione"
        bean = booking.getBookingFromSession();
        if (bean == null) {
            alert(Alert.AlertType.ERROR, "Dati mancanti", "Torna indietro e seleziona servizio e professionista.");
            backToPrevious();
            return;
        }

        // Riempi info statiche già note (cliente/professionista/servizio/prezzo)
        String nomeCliente = LoginController.getName();
        clienteValue.setText(nomeCliente == null ? "-" : nomeCliente);
        bean.setClienteId(LoginController.getId());

        professionistaValue.setText(bean.getBarbiereDisplay() == null ? "-" : bean.getBarbiereDisplay());

        // Recupera SOLO VM del servizio (niente entity)
        ServizioBean sVM = booking.getServizioVM(bean.getServizioId());
        servizioValue.setText(sVM == null || sVM.getName() == null ? "-" : sVM.getName());

        BigDecimal price = bean.getPrezzoTotale() == null ? BigDecimal.ZERO : bean.getPrezzoTotale();
        // Se mancasse il prezzo nella bean, prova dalla VM
        if (price.signum() == 0 && sVM != null && sVM.getPrice() != null) {
            price = sVM.getPrice();
            bean.setPrezzoTotale(price);
        }
        prezzoValue.setText(price.toPlainString() + " €");

        // Configura il DatePicker: date da OGGI in poi e SOLO giorni con slot disponibili
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(availableDayCellFactory());
        datePicker.valueProperty().addListener((obs, old, day) -> {
            populateTimesFor(day);
            updateSummary();
        });

        // Carica gli orari per la data iniziale
        populateTimesFor(datePicker.getValue());

        // Al cambio orario aggiorna riepilogo
        timeCombo.valueProperty().addListener((obs, old, t) -> updateSummary());
    }

    /** Crea una DayCell factory che disabilita date < oggi e date senza slot disponibili per barbiere+servizio. */
    private Callback<DatePicker, DateCell> availableDayCellFactory() {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                boolean disable = empty || item == null || item.isBefore(LocalDate.now());
                if (!disable) {
                    List<LocalTime> free = booking.listAvailableStartTimes(
                            bean.getBarbiereId(), item, bean.getServizioId()
                    );
                    if (free == null || free.isEmpty()) {
                        disable = true;
                        setTooltip(new Tooltip("Nessuna disponibilità"));
                    } else {
                        setTooltip(new Tooltip("Disponibili " + free.size() + " slot"));
                    }
                }
                setDisable(disable);
                if (disable) setStyle("-fx-background-color: #eee;");
            }
        };
    }

    /** Popola combo orari per il giorno selezionato. */
    private void populateTimesFor(LocalDate day) {
        timeCombo.getItems().clear();
        if (day == null) return;

        List<LocalTime> free = booking.listAvailableStartTimes(
                bean.getBarbiereId(), day, bean.getServizioId()
        );
        if (free == null || free.isEmpty()) {
            timeCombo.setPromptText("Nessun orario disponibile");
            orarioValue.setText("-");
            dataValue.setText(day.format(dateFmt));
            return;
        }
        timeCombo.getItems().addAll(free);
        timeCombo.setButtonCell(new TimeCell());
        timeCombo.setCellFactory(cb -> new TimeCell());

        // seleziona il primo per comodità
        timeCombo.getSelectionModel().selectFirst();

        // aggiorna bean e riepilogo
        bean.setDay(day);
        bean.setStartTime(timeCombo.getValue());
        ensureDurationFromVM(); // ricava durata da VM se mancante
        bean.computeEndTime();

        dataValue.setText(day.format(dateFmt));
        orarioValue.setText(timeCombo.getValue().format(timeFmt));
    }

    /** Aggiorna il riepilogo e sincronizza sulla bean. */
    private void updateSummary() {
        LocalDate d = datePicker.getValue();
        LocalTime t = timeCombo.getValue();
        if (d != null) {
            dataValue.setText(d.format(dateFmt));
            bean.setDay(d);
        }
        if (t != null) {
            orarioValue.setText(t.format(timeFmt));
            bean.setStartTime(t);
            ensureDurationFromVM();
            bean.computeEndTime();
        }
        booking.saveBookingToSession(bean);
    }

    /** Assicura che la durata sia presa dalla VM del servizio (no entity). */
    private void ensureDurationFromVM() {
        if (bean.getDurataTotaleMin() > 0) return;
        ServizioBean sVM = booking.getServizioVM(bean.getServizioId());
        if (sVM != null && sVM.getDurationMin() > 0) {
            bean.setDurataTotaleMin(sVM.getDurationMin());
        }
    }

    @FXML
    private void onPaga() throws IOException {
        if (bean.getDay() == null || bean.getStartTime() == null) {
            alert(Alert.AlertType.WARNING, "Dati incompleti", "Seleziona giorno e orario.");
            return;
        }

        // Verifica che lo slot sia ancora libero
        List<LocalTime> free = booking.listAvailableStartTimes(
                bean.getBarbiereId(), bean.getDay(), bean.getServizioId()
        );
        if (free.stream().noneMatch(t -> t.equals(bean.getStartTime()))) {
            alert(Alert.AlertType.ERROR, "Slot non più disponibile", "Seleziona un altro orario.");
            populateTimesFor(bean.getDay());
            return;
        }

        WindowManager.getInstance().switchScene("PagamentoViewAlternative.fxml","Pagamento");
        alert(Alert.AlertType.INFORMATION, "OK", "Procedi al pagamento / conferma.");
    }

    @FXML
    private void onBack() {
        try {
            WindowManager.getInstance().switchScene("PrenotazioneViewAlternative.fxml", "Scegli Servizio");
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Errore", "Impossibile tornare alla selezione servizio.");
        }
    }

    private void backToPrevious() { onBack(); }

    private void alert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle("Prenotazione");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    /** Renderer per LocalTime nella ComboBox. */
    private class TimeCell extends ListCell<LocalTime> {
        @Override protected void updateItem(LocalTime t, boolean empty) {
            super.updateItem(t, empty);
            setText(empty || t == null ? null : t.format(timeFmt));
        }
    }
}
