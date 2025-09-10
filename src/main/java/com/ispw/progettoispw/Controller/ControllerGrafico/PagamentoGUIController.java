package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.*; // se lo usi per punti, opzionale
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.PaymentBean;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.pattern.WindowManager;

// Facade pagamento (interfaccia ad alto livello che userai “dopo Pago”)


// Validazione coupon
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class PagamentoGUIController {

    @FXML private Pane root;

    // Campi carta
    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;   // MM/YY
    @FXML private PasswordField cvvField;

    // Coupon
    @FXML private TextField couponField;
    @FXML private Button validateCouponButton;

    // Totale / errori
    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    // Bottoni azione
    @FXML private Button payButton;
    @FXML private Button backButton;

    // Controller applicativi
    private final BookingController bookingController = new BookingController();
    private final CouponController  couponController  = new CouponController();   // <-- usa il tuo
    private final LoginController login= new LoginController();

    // Stato
    private BookingBean booking;           // dalla sessione
    private BigDecimal baseTotal = BigDecimal.ZERO; // totale iniziale (prezzo servizio)
    private BigDecimal currentTotal = BigDecimal.ZERO; // totale attuale (dopo coupon)
    private CouponBean cb = new CouponBean();
    @FXML
    private void initialize() {
        hideError();

        // Recupero BookingBean dallo step precedente (via sessione)
        booking = bookingController.getBookingFromSession();
        if (booking == null || booking.getServizioId() == null) {
            showError("Dati prenotazione mancanti. Torna allo step precedente.");
            disableAll();
            return;
        }

        // Calcola il totale base dal servizio scelto
        if (booking.getPrezzoTotale() != null) {
            baseTotal = booking.getPrezzoTotale();
        } else {
            // fallback: leggi prezzo dal servizio via BookingController
            var s = bookingController.getServizio(booking.getServizioId());
            baseTotal = (s != null && s.getPrice() != null) ? s.getPrice() : BigDecimal.ZERO;
        }
        currentTotal = baseTotal;
        refreshTotalLabel();

        // Listener bottone valida coupon

    }

    /* ========================= Coupon ========================= */
    @FXML
    private void onValidateCoupon() {
        hideError();

        String code = couponField.getText() == null ? "" : couponField.getText().trim();
        if (code.isEmpty()) {
            showError("Inserisci un codice coupon.");
            return;
        }

        try {
            // Prepara la bean per coupon

            cb.setClienteId(booking.getClienteId()); // se vuoi vincolare al cliente
            cb.setCouponCode(code);

            // Chiedi al CouponController il nuovo totale
            // ⬇️ ADEGUA questa chiamata alla tua firma concreta
            BigDecimal newTotal = couponController.previewTotalWithCoupon(cb, baseTotal);

            if (newTotal == null || newTotal.equals(currentTotal)) {
                showError("Coupon non valido o scaduto.");
                return;
            }
            if (newTotal.compareTo(baseTotal) > 0) {
                // per sicurezza: non deve mai aumentare
                showError("Coupon non valido.");
                return;
            }

            currentTotal = newTotal;
            refreshTotalLabel();

        } catch (Exception ex) {
            showError("Coupon non valido: " + ex.getMessage());
        }
    }

    /* ========================= Paga (Facade) ========================= */
    @FXML
    private void onPay() {
        hideError();

        // Prepara payment bean dalla UI
        PaymentBean pb = new PaymentBean();
        pb.setCardHolderName(safeText(cardHolderField));
        pb.setCardNumber(safeText(cardNumberField));
        pb.setExpiry(safeText(expiryField));
        pb.setCvv(safeText(cvvField));
        pb.setCouponCode(safeText(couponField));
        pb.setAmount(currentTotal);
        booking.setOnline();
        booking.setPrezzoTotale(currentTotal);
        booking.setCouponCode(safeText(couponField));
        // Valida lato client (numero carta, scadenza, CVV, importo, ecc.)
        List<String> errs = pb.validate();
        if (!errs.isEmpty()) {
            showError(String.join("\n", errs));
            return;
        }

        // Esegui il processo di pagamento tramite Facade (astrai la complessità)
        // La Facade dovrebbe: autorizzare carta, applicare coupon, salvare appuntamento (se lo vuoi qui),
        // accreditare punti, inviare email, ecc. — a seconda del tuo flow.
        try {
            String result = bookingController.paga(pb.getCardHolderName(), pb.getCardNumber(), pb.getExpiry(), pb.getCvv(),pb.getAmount());
            switch (result) {
                case "success" -> {

                    bookingController.book(booking);
                    // Se qui salvi e chiudi il flusso, puoi ripulire la sessione:
                    couponController.markCouponUsed(cb.getCouponCode(), cb.getClienteId());

                    bookingController.sendEmail(booking);
                    bookingController.clearBookingFromSession();
                    showInfo("Pagamento completato con successo!");
                    goHome();
                }
                case "error:card_declined" -> showError("Carta rifiutata. Controlla i dati o usa un altro metodo.");
                case "error:coupon_invalid" -> showError("Coupon non valido al momento del pagamento.");
                case "error:slot_taken" -> showError("L'orario selezionato non è più disponibile.");
                default -> showError("Errore durante il pagamento. Riprova.");
            }
        } catch (Exception ex) {
            showError("Errore inatteso: " + ex.getMessage());
        }
    }

    /* ========================= Navigazione ========================= */
    @FXML
    private void onBack() {
        try {
            WindowManager.getInstance().switchScene("RiepilogoView.fxml", "Riepilogo");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void goHome() {
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ========================= Helpers UI ========================= */

    private void refreshTotalLabel() {
        if (totalLabel != null) {
            totalLabel.setText("€ " + currentTotal.toPlainString());
        }
    }

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }

    private void disableAll() {
        if (root != null) root.setDisable(true);
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static String safeText(TextField tf) {
        return tf == null ? null : tf.getText();
    }
}
