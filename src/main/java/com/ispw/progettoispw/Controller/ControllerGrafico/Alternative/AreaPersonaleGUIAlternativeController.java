package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AreaPersonaleGUIAlternativeController {

    // --- UI (ricorda di aggiungere gli fx:id al tuo FXML) ---
    @FXML private ListView<BookingBean> listView;
    @FXML private Button backButton;
    @FXML private Button fidelityButton;

    // --- controller applicativi ---
    private final BookingController bookingController = new BookingController();
    private final LoginController   loginController   = new LoginController();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private final ObservableList<BookingBean> items = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // setup list
        listView.setItems(items);
        listView.setPlaceholder(new Label("Nessuna prenotazione trovata."));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookingBean b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String data   = b.getDay()       != null ? b.getDay().format(dateFmt) : "-";
                String inizio = b.getStartTime() != null ? b.getStartTime().format(timeFmt) : "-";
                String fine   = b.getEndTime()   != null ? b.getEndTime().format(timeFmt) : "-";
                String prezzo = b.getPrezzoTotale() != null ? b.getPrezzoTotale().toPlainString() + " €" : "-";
                String stato  = b.getStatus() != null ? b.getStatus().name() : "-";
                String svc    = b.getServiceName() != null ? b.getServiceName() : "Servizio";
                String coupon = b.getCouponCode() != null ? b.getCouponCode() : "-";

                Label info = new Label(
                        svc + " | " + data + " " + inizio + "-" + fine +
                                " | Prezzo: " + prezzo + " | Coupon: " + coupon + " | Stato: " + stato
                );
                Button cancel = new Button("Cancella");
                cancel.setStyle("-fx-background-color:#ff0b0b; -fx-text-fill:white;");
                cancel.setOnAction(e -> onCancel(b));

                HBox row = new HBox(10, info, cancel);
                row.setFillHeight(true);
                setGraphic(row);
                setText(null);
            }
        });

        // carica i dati
        loadAppointments();
    }

    private void loadAppointments() {
        items.clear();

        String clientId = loginController.getId(); // serve ID cliente
        if (clientId == null || clientId.isBlank()) {
            listView.setPlaceholder(new Label("Utente non loggato."));
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            listView.setPlaceholder(new Label("Nessuna prenotazione trovata."));
            return;
        }
        items.addAll(lista);
    }

    private void onCancel(BookingBean b) {
        if (b.getStatus() == AppointmentStatus.CANCELLED) {
            info("Prenotazione già cancellata.");
            return;
        }
        if (b.getAppointmentId() == null) {
            info("Impossibile cancellare: id appuntamento mancante.");
            return;
        }
        boolean ok = bookingController.cancelCustomerAppointment(b.getAppointmentId());
        if (!ok) {
            info("Impossibile cancellare la prenotazione.");
            return;
        }
        info("Prenotazione cancellata.");
        loadAppointments();
    }

    // --- NAVIGAZIONE ---

    @FXML
    private void onBack() {
        try {
            // Cambia il path in base alla tua struttura risorse
            WindowManager.getInstance().switchScene("HomeViewAlternative.fxml", "Home");
        } catch (IOException e) {
            error("Caricamento fallito", "Impossibile aprire la schermata Home.");
        }
    }

    @FXML
    private void onFidelity() {
        try {
            // Cambia con il tuo path reale della fidelity (es. "view/FidelityCard.fxml")
            WindowManager.getInstance().switchScene("FidelityCardViewAlternative.fxml", "Fidelity Card");
        } catch (IOException e) {
            error("Caricamento fallito", "Impossibile aprire la schermata Fidelity Card.");
        }
    }

    // --- util ---

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void error(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
