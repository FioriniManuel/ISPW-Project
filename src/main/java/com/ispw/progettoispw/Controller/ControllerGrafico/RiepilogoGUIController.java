package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean; // <- semplice VM: id, name, price, duration (nessuna entity)
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class RiepilogoGUIController {

    @FXML private StackPane root;

    @FXML private Label clienteLabel;
    @FXML private Label barbiereLabel;
    @FXML private Label dataLabel;
    @FXML private Label orarioLabel;
    @FXML private Label servizioLabel;
    @FXML private Label prezzoLabel;

    @FXML private ToggleGroup paymentGroup;
    @FXML private RadioButton payInShopRadio;
    @FXML private RadioButton payInAppRadio;

    @FXML private Button confermaButton;
    @FXML private Button indietroButton;

    private final BookingController bookingController = new BookingController();
    private final LoginController login = new LoginController();

    private BookingBean bean;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    @FXML
    private void initialize() {
        bean = bookingController.getBookingFromSession();
        if (bean == null) {
            showWarn("Dati prenotazione mancanti. Torna allo step precedente.");
            disableAll();
            return;
        }
        refreshView();
    }

    private void refreshView() {
        // Cliente
        String clienteName = LoginController.getName();
        clienteLabel.setText(clienteName == null ? "-" : clienteName);
        bean.setClienteId(LoginController.getId());

        // Barbiere (usa displayName messo allo step precedente)
        String barberDisplay = bean.getBarbiereDisplay();
        if (barberDisplay == null || barberDisplay.isBlank()) barberDisplay = bean.getBarbiereId();
        barbiereLabel.setText(barberDisplay == null ? "-" : barberDisplay);

        // Data/Ora
        dataLabel.setText(bean.getDay() == null ? "-" : bean.getDay().format(DATE_FMT));
        if (bean.getStartTime() != null && bean.getEndTime() != null) {
            orarioLabel.setText(bean.getStartTime().format(TIME_FMT) + " - " + bean.getEndTime().format(TIME_FMT));
        } else {
            orarioLabel.setText("-");
        }

        // Servizio + Prezzo (usa valori già messi nella bean; altrimenti fallback su VM dal controller)
        String serviceName = bean.getServiceName();
        BigDecimal price   = bean.getPrezzoTotale();

        if ((serviceName == null || serviceName.isBlank()) || price == null) {
            // fallback soft: prova a leggere la VM (NO entity)
            String sid = bean.getServizioId();
            if (sid != null && !sid.isBlank()) {
                ServizioBean sb = bookingController.getServizioVM(sid); // <-- questo metodo deve restituire una VM
                if (sb != null) {
                    if (serviceName == null || serviceName.isBlank()) {
                        serviceName = sb.getName();
                        bean.setServiceName(serviceName);
                    }
                    if (price == null) {
                        price = (sb.getPrice() == null ? BigDecimal.ZERO : sb.getPrice());
                        bean.setPrezzoTotale(price);
                    }
                }
            }
        }

        servizioLabel.setText(serviceName == null ? "-" : serviceName);
        prezzoLabel.setText("€ " + PRICE_FMT.format(price == null ? BigDecimal.ZERO : price));
    }

    @FXML
    private void onConferma() {
        if (bean == null) return;
        if (paymentGroup.getSelectedToggle() == null) {
            showWarn("Seleziona una modalità di pagamento.");
            return;
        }

        boolean payInApp = (paymentGroup.getSelectedToggle() == payInAppRadio);

        if (payInApp) {
            // Pagamento in app: NON salvo l'appuntamento qui. Verifico solo che i dati siano validi.
            bean.setOnline();
            String esito = bookingController.bookOnline(bean);
            switch (esito) {
                case "success" -> {
                    bookingController.saveBookingToSession(bean);
                    showInfo("Procedo al pagamento in app…");
                    try {
                        WindowManager.getInstance().switchScene("PagamentoView.fxml", "Pagamento");
                    } catch (IOException e) {
                        showWarn("Impossibile aprire la schermata di pagamento.");
                    }
                }
                case "error:slot_taken" -> showWarn("L'orario scelto non è più disponibile. Seleziona un nuovo orario.");
                case "error:validation" -> showWarn("Dati prenotazione non validi. Controlla e riprova.");
                default -> showWarn("Errore imprevisto: " + esito);
            }
        } else {
            // Pagamento in sede: salvo subito l'appuntamento
            bean.setInsede();
            String esito = bookingController.book(bean);
            switch (esito) {
                case "success" -> {
                    showInfo("Prenotazione confermata. Pagherai in sede.");
                    bookingController.sendEmail(bean);
                    bookingController.clearBookingFromSession();
                    try {
                        WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
                    } catch (IOException e) {
                        showWarn("Impossibile tornare alla Home.");
                    }
                }
                case "error:slot_taken" -> showWarn("L'orario scelto non è più disponibile. Seleziona un nuovo orario.");
                case "error:validation" -> showWarn("Dati prenotazione non validi. Controlla e riprova.");
                default -> showWarn("Errore imprevisto: " + esito);
            }
        }
    }

    @FXML
    private void onIndietro() {
        try {
            WindowManager.getInstance().switchScene("OrarioView.fxml", "Scegli orario");
        } catch (IOException e) {
            showWarn("Impossibile aprire la schermata precedente.");
        }
    }

    // ------- helpers -------
    private void disableAll() { if (root != null) root.setDisable(true); }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}

