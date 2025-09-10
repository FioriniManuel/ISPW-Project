package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.UnaryOperator;

public class HomeBarbiereGUIAlternativeController {

    @FXML private Label nomeBarbiere;
    @FXML private Button btnEsci;

    // NUOVI: campo data e lista appuntamenti
    @FXML private TextField dateTextField;          // formato dd/MM/yyyy
    @FXML private ListView<BookingBean> appointmentsList;

    private final LoginController   login             = new LoginController();
    private final BookingController bookingController = new BookingController();
    private final CouponController  couponController  = new CouponController();

    private String barberId;   // vero id del barbiere (preferito)
    private String barberName; // display

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        // --- intestazione/nome
        this.barberId   = login.getId();        // usa l'ID reale dalla sessione
        this.barberName = LoginController.getName();

        nomeBarbiere.setText((barberName != null && !barberName.isBlank()) ? barberName : "Barbiere");
        btnEsci.setOnAction(e -> doLogout());

        // --- placeholder lista
        if (appointmentsList != null) {
            appointmentsList.setPlaceholder(new Label("Nessuna prenotazione per la data selezionata."));
        }

        // --- formatter per data (dd/MM/yyyy) con input “guidato”
        if (dateTextField != null) {
            dateTextField.setTextFormatter(new TextFormatter<>(dateMaskFilter()));
            // default: oggi
            LocalDate today = LocalDate.now();
            dateTextField.setText(DF.format(today));
            // aggiorna onAction (ENTER) e onFocusLost
            dateTextField.setOnAction(e -> applyDateFromField());
            dateTextField.focusedProperty().addListener((obs, was, is) -> {
                if (!is) applyDateFromField();
            });
        }

        // --- cell factory: stessa UX di PrenotazioniTabGUIController
        if (appointmentsList != null) {
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
                            info("Appuntamento già cancellato.");
                            return;
                        }
                        if (b.getStatus() == AppointmentStatus.COMPLETED) {
                            info("Appuntamento già completato.");
                            return;
                        }
                        if (bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED)) {
                            b.setStatus(AppointmentStatus.COMPLETED);

                            // accredita punti in base al prezzo
                            int pt = couponController.computePointsToAward(b.getPrezzoTotale());
                            if (b.getClienteId() != null) {
                                couponController.addPointsToLoyalty(b.getClienteId(), pt);
                            }
                            refreshRow(b);
                        } else {
                            info("Impossibile aggiornare lo stato (riprovare).");
                        }
                    });

                    cancelBtn.setOnAction(e -> {
                        BookingBean b = getItem();
                        if (b == null) return;

                        if (b.getStatus() == AppointmentStatus.CANCELLED) {
                            info("Appuntamento già cancellato.");
                            return;
                        }
                        if (b.getStatus() == AppointmentStatus.COMPLETED) {
                            info("Impossibile cancellare: appuntamento già completato.");
                            return;
                        }
                        if (bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.CANCELLED)) {
                            b.setStatus(AppointmentStatus.CANCELLED);
                            refreshRow(b);
                        } else {
                            info("Impossibile aggiornare lo stato (riprovare).");
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
        }

        // carica appuntamenti “oggi”
        applyDateFromField();
    }

    /** Filtro input mascherato per data dd/MM/yyyy (permette digitazione step-by-step). */
    private UnaryOperator<Change> dateMaskFilter() {
        // consenti solo numeri e slash, con struttura fino a 10 char: dd/MM/yyyy
        return c -> {
            String newText = c.getControlNewText();
            if (newText.length() > 10) return null;
            if (!newText.matches("[0-9/]*")) return null;
            // vincolo posizioni degli slash (opzionale, tollerante)
            return c;
        };
    }

    /** Legge la data dal TextField, valida e carica lista appuntamenti; se invalida, ripristina oggi. */
    private void applyDateFromField() {
        if (appointmentsList == null || dateTextField == null) return;

        String raw = dateTextField.getText() == null ? "" : dateTextField.getText().trim();
        LocalDate day;
        try {
            day = LocalDate.parse(raw, DF);
        } catch (Exception ex) {
            // ripristina oggi se formato invalido
            day = LocalDate.now();
            dateTextField.setText(DF.format(day));
            warn("Formato data non valido. Usa dd/MM/yyyy (es. " + DF.format(day) + ").");
        }

        loadAppointments(day);
    }

    private void loadAppointments(LocalDate day) {
        if (barberName == null || barberName.isBlank()) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            return;
        }
        List<BookingBean> list = bookingController.listAppointmentsForBarberOnDayVM(barberId, day);
        appointmentsList.setItems(FXCollections.observableArrayList(list));
    }

    private String buildRowText(BookingBean b) {
        String nomeServizio = (b.getServiceName() != null && !b.getServiceName().isBlank())
                ? b.getServiceName() : "Servizio";
        String range = (b.getStartTime() == null || b.getEndTime() == null)
                ? "-" : (b.getStartTime().format(TF) + "-" + b.getEndTime().format(TF));
        String price = (b.getPrezzoTotale() == null) ? "-" : (b.getPrezzoTotale().toPlainString() + " €");
        String stato = (b.getStatus() == null) ? "-" : b.getStatus().name();
        return nomeServizio + " | " + range + " | " + price + " | " + stato;
    }

    @FXML
    private void notImplementedOnAction() {
        try {
            WindowManager.getInstance().switchScene(
                    "NotImplementedView.fxml",
                    "Gestione Fidelity"
            );
        } catch (IOException ex) {
            error("Impossibile aprire la Gestione Fidelity: " + ex.getMessage());
        }
    }

    private void doLogout() {
        try {
            login.logOut();
            WindowManager.getInstance().switchScene("LoginViewAlternative.fxml", "Zac Zac");
        } catch (IOException ex) {
            error("Errore durante il logout: " + ex.getMessage());
        }
    }

    // --- util alert ---
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
}
