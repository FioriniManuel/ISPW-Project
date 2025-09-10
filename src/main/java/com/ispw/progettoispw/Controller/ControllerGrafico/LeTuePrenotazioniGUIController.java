package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.AppointmentStatus;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeTuePrenotazioniGUIController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox prenotazioniBox;
    @FXML private Button esciLeTuePrenotazioni;

    private final BookingController bookingController = new BookingController();
    private final CouponController  couponController  = new CouponController();
    private final LoginController   login             = new LoginController();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        loadPrenotazioni();
    }

    private void loadPrenotazioni() {
        prenotazioniBox.getChildren().clear();

        String clientId = login.getId(); // ⚠️ serve l’ID, non il nome
        if (clientId == null || clientId.isBlank()) {
            prenotazioniBox.getChildren().add(new Label("Utente non loggato."));
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            prenotazioniBox.getChildren().add(new Label("Nessuna prenotazione trovata."));
            return;
        }

        for (BookingBean b : lista) {
            String data   = b.getDay().format(dateFmt);
            String inizio = b.getStartTime().format(timeFmt);
            String fine   = b.getEndTime().format(timeFmt);
            String prezzo = b.getPrezzoTotale() == null ? "-" : b.getPrezzoTotale().toPlainString() + " €";
            String stato  = b.getStatus() == null ? "-" : b.getStatus().name();

            String Coupon = b.getCouponCode() == null ? "-" : b.getCouponCode();

            Label info = new Label(
                    (b.getServiceName() == null ? "Servizio" : b.getServiceName())
                            + " | " + data + " " + inizio + "-" + fine
                            + " | Prezzo: " + prezzo + " | Coupon: "+ Coupon+ " | Stato: " + stato
            );

            Button cancelBtn = new Button("Cancella");
            cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
            cancelBtn.setOnAction(ev -> onCancelAppointment(b));

            HBox row = new HBox(10, info, cancelBtn);
            prenotazioniBox.getChildren().add(row);
        }
    }

    private void onCancelAppointment(BookingBean b) {
        if (b.getStatus() == AppointmentStatus.CANCELLED) {
            showInfo("Prenotazione già cancellata.");
            return;
        }
        if (b.getAppointmentId() == null) {
            showInfo("Impossibile cancellare: id appuntamento mancante.");
            return;
        }

        boolean ok = bookingController.cancelCustomerAppointment(b.getAppointmentId());
        if (!ok) {
            showInfo("Impossibile cancellare la prenotazione.");
            return;
        }

        // Se gestisci coupon applicati, qui potresti riattivarli
        // (servirebbe avere appliedCouponId sul BookingBean, se ti serve)
        // es: couponController.reactivateCoupon(b.getAppliedCouponId(), b.getClienteId());

        showInfo("Prenotazione cancellata.");
        loadPrenotazioni();
    }

    @FXML
    private void esciLeTuePrenotazionionAction() {
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
