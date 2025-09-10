package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrarioGUIController {

    @FXML private StackPane root;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;
    @FXML private ComboBox<BarbiereBean> barberCombo; // <-- VM, non entity
    @FXML private Label warningLabel;

    private final BookingController bookingController = new BookingController();
    private BookingBean bookingBean; // letta da SessionManager

    @FXML
    private void initialize() {
        warningLabel.setVisible(false);

        // Recupera la bean dalla sessione
        bookingBean = bookingController.getBookingFromSession();
        if (bookingBean == null || bookingBean.getServizioId() == null || bookingBean.getServizioId().isBlank()) {
            showWarn("Dati prenotazione mancanti. Torna allo step precedente.");
            return;
        }

        // Imposta data di default: oggi
        datePicker.setValue(LocalDate.now());

        // Consenti solo oggi e future
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) return;
                setDisable(d.isBefore(LocalDate.now()));
            }
        });

        // Listeners
        datePicker.valueProperty().addListener((obs, oldD, newD) -> onDateChanged(newD));
        barberCombo.valueProperty().addListener((obs, oldB, newB) -> onBarberChanged(newB));

        // Rendering VM in combo
        barberCombo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(BarbiereBean vm, boolean empty) {
                super.updateItem(vm, empty);
                setText(empty || vm == null ? null : vm.getDisplayName());
            }
        });
        barberCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(BarbiereBean vm, boolean empty) {
                super.updateItem(vm, empty);
                setText(empty || vm == null ? null : vm.getDisplayName());
            }
        });

        // Orari HH:mm
        DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
        timeCombo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.format(HHMM));
            }
        });
        timeCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.format(HHMM));
            }
        });

        // Trigger iniziale
        onDateChanged(datePicker.getValue());
    }

    public void onDateChanged(LocalDate newDate) {
        clearWarning();
        timeCombo.getItems().clear();

        if (newDate == null) {
            barberCombo.getItems().clear();
            return;
        }

        List<BarbiereBean> disponibili = bookingController.availableBarbersVM(newDate, bookingBean.getServizioId());
        barberCombo.setItems(FXCollections.observableArrayList(disponibili));

        if (disponibili.isEmpty()) {
            showWarn("Nessun professionista disponibile in questa data.");
        }
    }

    public void onBarberChanged(BarbiereBean newBarber) {
        clearWarning();
        timeCombo.getItems().clear();

        LocalDate day = datePicker.getValue();
        if (newBarber == null || day == null) {
            if (day == null) showWarn("Seleziona prima una data.");
            return;
        }

        // Orari disponibili per barbiere/giorno/servizio (usa l'ID vero della VM!)
        List<LocalTime> libres = bookingController
                .listAvailableStartTimes(newBarber.getId(), day, bookingBean.getServizioId());
        timeCombo.setItems(FXCollections.observableArrayList(libres));

        if (libres.isEmpty()) {
            showWarn("Nessun orario disponibile per il professionista selezionato.");
        }
    }

    /* ===== Bottoni ===== */

    @FXML
    public void listinoButtonOnAction() {
        bookingController.clearBookingFromSession();
        try {
            WindowManager.getInstance().switchScene("PrenotazioneView.fxml", "Listino");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void congratulazioniButtonOnAction() {
        clearWarning();

        LocalDate day = datePicker.getValue();
        BarbiereBean barber = barberCombo.getValue();
        LocalTime start = timeCombo.getValue();

        if (day == null)  { showWarn("Seleziona una data."); return; }
        if (barber == null){ showWarn("Seleziona un professionista."); return; }
        if (start == null) { showWarn("Seleziona un orario."); return; }

        // aggiorna la bean in sessione
        bookingBean.setDay(day);
        bookingBean.setBarbiereId(barber.getId());
        bookingBean.setBarbiereDisplay(barber.getDisplayName());// <-- salva l'ID, NON "nome cognome"
        bookingBean.setStartTime(start);
        bookingBean.computeEndTime();

        bookingController.saveBookingToSession(bookingBean);

        try {
            WindowManager.getInstance().switchScene("RiepilogoView.fxml", "Riepilogo");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ===== Helpers UI ===== */

    private void showWarn(String msg) {
        if (warningLabel != null) {
            warningLabel.setText(msg);
            warningLabel.setVisible(true);
        } else {
            new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
        }
    }

    private void clearWarning() {
        if (warningLabel != null) {
            warningLabel.setVisible(false);
            warningLabel.setText("");
        }
    }
}
