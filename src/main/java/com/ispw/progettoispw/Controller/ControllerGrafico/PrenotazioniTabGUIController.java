package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrenotazioniTabGUIController {

    @FXML private DatePicker datePicker;
    @FXML private Label dateLabel;
    @FXML private ListView<BookingBean> appointmentsList;

    private final LoginController login = new LoginController();
    private final CouponController couponController = new CouponController();
    private final BookingController bookingController = new BookingController();

    private String barberId;


    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        // UI base
        appointmentsList.setPlaceholder(new Label("Nessuna prenotazione per la data selezionata."));
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        dateLabel.setText(df.format(today));

        barberId=login.getId();

        // Se initForBarber NON viene chiamato dal parent, recupero dalla sessione


        // Listener data (viene risettato dentro initForBarber, ma se non venisse chiamato gestiamo anche qui)
        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                dateLabel.setText(df.format(newV));
                loadAppointments(newV);
            }
        });


        // cell factory con pulsanti "Completa" e "Cancella"
        appointmentsList.setCellFactory(list -> new ListCell<>() {
            private final Label lbl = new Label();
            private final Button completeBtn = new Button("Completa");
            private final Button cancelBtn = new Button("Cancella");
            private final HBox box = new HBox(10, lbl, completeBtn, cancelBtn);

            {
                cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
                completeBtn.setStyle("-fx-background-color: #63c755; -fx-text-fill: white;");

                completeBtn.setOnAction(e -> {
                    BookingBean b = getItem();
                    if (b == null) return;

                    if (b.getStatus() == AppointmentStatus.CANCELLED) {
                        showInfo("Appuntamento già cancellato.");
                        return;
                    }
                    if (b.getStatus() == AppointmentStatus.COMPLETED) {
                        showInfo("Appuntamento già completato.");
                        return;
                    }
                    if (bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED)) {
                        b.setStatus(AppointmentStatus.COMPLETED);
                        // accredita punti (se prevedi ciò lato barbiere)

                        int pt = couponController.computePointsToAward(b.getPrezzoTotale());
                        couponController.addPointsToLoyalty(b.getClienteId(), pt);
                        refreshRow(b);
                    } else {
                        showInfo("Impossibile aggiornare lo stato (riprovare).");
                    }
                });

                cancelBtn.setOnAction(e -> {
                    BookingBean b = getItem();
                    if (b == null) return;

                    if (b.getStatus() == AppointmentStatus.CANCELLED) {
                        showInfo("Appuntamento già cancellato.");
                        return;
                    }
                    if (b.getStatus() == AppointmentStatus.COMPLETED) {
                        showInfo("Impossibile cancellare: appuntamento già completato.");
                        return;
                    }
                    if (bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.CANCELLED)) {
                        b.setStatus(AppointmentStatus.CANCELLED);
                        refreshRow(b);
                    } else {
                        showInfo("Impossibile aggiornare lo stato (riprovare).");
                    }
                });
            }

            @Override
            protected void updateItem(BookingBean b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lbl.setText(buildRowText(b));
                    completeBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                            || b.getStatus() == AppointmentStatus.COMPLETED);
                    cancelBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                            || b.getStatus() == AppointmentStatus.COMPLETED);
                    setGraphic(box);
                }
            }

            private void refreshRow(BookingBean b) {
                lbl.setText(buildRowText(b));
                completeBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                        || b.getStatus() == AppointmentStatus.COMPLETED);
                cancelBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                        || b.getStatus() == AppointmentStatus.COMPLETED);
            }
        });

        // carica appuntamenti iniziali
        loadAppointments(today);
    }

    private void loadAppointments(LocalDate day) {
        if (barberId == null || barberId.isBlank()) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            return;
        }
        List<BookingBean> list = bookingController.listAppointmentsForBarberOnDayVM(barberId, day);
        appointmentsList.setItems(FXCollections.observableArrayList(list));
    }

    private String buildRowText(BookingBean b) {
        String nomeServizio = (b.getServiceName() != null && !b.getServiceName().isBlank())
                ? b.getServiceName() : "Servizio";
        String range = b.getStartTime().format(tf) + "-" + b.getEndTime().format(tf);
        String price = b.getPrezzoTotale() == null ? "-" : (b.getPrezzoTotale().toPlainString() + " €");
        String stato = b.getStatus() == null ? "-" : b.getStatus().name();
        return nomeServizio + " | " + range + " | " + price + " | " + stato;
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    public void indietroButtonOnAction() throws IOException {
        WindowManager.getInstance().switchScene("HomeBarbiereView.fxml", "Home");
    }
}
