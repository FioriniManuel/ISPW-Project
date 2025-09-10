package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoyaltyController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.bean.PaymentBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class PagamentoGUIAlternativeController {

    @FXML private Pane root;

    // RADIO
    @FXML private RadioButton pagaOnline;
    @FXML private RadioButton pagaInSede;

    // CONTENITORI DA MOSTRARE/NASCONDERE
    @FXML private Pane cardPane;        // riquadro dati carta
    @FXML private GridPane couponGrid;  // blocco coupon
    // riepilogo cliente/prof/...

    // Campi carta
    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;

    // Coupon
    @FXML private TextField couponField;

    // Totale / messaggi
    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    // Riepilogo (già nell’FXML con fx:id)


    // Bottoni
    @FXML private Button backButton;
    @FXML private Button payButton;

    private final BookingController bookingController = new BookingController();
    private final CouponController couponController   = new CouponController();
    private final LoyaltyController loyaltyController = new LoyaltyController();

    private BookingBean booking;             // dalla sessione
    private BigDecimal baseTotal   = BigDecimal.ZERO; // prezzo base servizio
    private BigDecimal currentTotal = BigDecimal.ZERO; // totale visualizzato (aggiornato subito)

    private final ToggleGroup payGroup = new ToggleGroup();
    private final CouponBean couponBean = new CouponBean();

    @FXML
    private void initialize() {
        hideError();

        // Toggle group
        pagaOnline.setToggleGroup(payGroup);
        pagaInSede.setToggleGroup(payGroup);

        // Recupero booking dalla sessione
        booking = bookingController.getBookingFromSession();
        if (booking == null || booking.getServizioId() == null) {
            showError("Dati prenotazione mancanti. Torna allo step precedente.");
            disableAll();
            return;
        }

        // Calcolo importi
        if (booking.getPrezzoTotale() != null) {
            baseTotal = booking.getPrezzoTotale();
        } else {
            var s = bookingController.getServizio(booking.getServizioId());
            baseTotal = (s != null && s.getPrice() != null) ? s.getPrice() : BigDecimal.ZERO;
        }
        currentTotal = baseTotal;
        refreshTotalLabel();

        // Riepilogo statico (quello in griglia)


        // Stato iniziale: nascondo entrambi i blocchi, mostro solo recap
        showInSedeMode(true);  // di default mostriamo recap (in sede)
        pagaInSede.setSelected(true);

        payGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean inSede = (newT == pagaInSede);
            showInSedeMode(inSede);
        });
    }

    /** Mostra/nasconde contenuti in base alla modalità selezionata. */
    private void showInSedeMode(boolean inSede) {
        // Riepilogo: sempre visibile


        // Blocco coupon + carta: visibili solo per ONLINE
        boolean showOnlineFields = !inSede;
        if (couponGrid != null)  couponGrid.setVisible(showOnlineFields);
        if (cardPane != null)    cardPane.setVisible(showOnlineFields);

        // Pulizia errori
        hideError();

        // Reset totale se cambio modalità (il totale mostrato è sempre quello corrente)
        currentTotal = (showOnlineFields ? currentTotal : baseTotal);
        refreshTotalLabel();

        // Testo bottone
        payButton.setText(inSede ? "Conferma Prenotazione" : "Paga");
    }

    /* ===================== Coupon: aggiorna SUBITO il totale ===================== */
    @FXML
    private void onValidateCoupon() {
        hideError();

        if (!pagaOnline.isSelected()) {
            showError("La validazione coupon è disponibile solo per il pagamento online.");
            return;
        }

        String code = safeText(couponField);
        if (code.isBlank()) {
            showError("Inserisci un codice coupon.");
            return;
        }

        try {
            couponBean.setClienteId(booking.getClienteId());
            couponBean.setCouponCode(code);

            BigDecimal newTotal = couponController.previewTotalWithCoupon(couponBean, baseTotal);
            // Aggiorna SUBITO l’importo mostrato
            currentTotal = newTotal != null ? newTotal : baseTotal;
            refreshTotalLabel();

            if (newTotal == null || newTotal.compareTo(baseTotal) == 0) {
                showError("Coupon non valido o nessuno sconto applicabile.");
            }
        } catch (Exception ex) {
            showError("Errore validazione coupon: " + ex.getMessage());
        }
    }

    /* ===================== PAGA / CONFERMA ===================== */
    @FXML
    private void onPay() {
        hideError();

        if (pagaInSede.isSelected()) {
            // Flusso IN SEDE: niente carta/coupon, stato PENDING
            booking.setInsede();
            booking.setPrezzoTotale(baseTotal);        // prezzo pieno
            booking.setCouponCode(null);               // nessun coupon in sede

            // Anti overbooking prima del salvataggio
            var free = bookingController.listAvailableStartTimes(
                    booking.getBarbiereId(), booking.getDay(), booking.getServizioId());
            if (free == null || free.stream().noneMatch(t -> t.equals(booking.getStartTime()))) {
                showError("Lo slot selezionato non è più disponibile. Torna indietro e scegli un altro orario.");
                return;
            }

            String res = bookingController.book(booking);
            if (!"success".equalsIgnoreCase(res)) {
                showError(switch (res) {
                    case "error:slot_taken" -> "Orario non disponibile.";
                    case "error:validation" -> "Dati mancanti o non validi.";
                    default -> "Impossibile completare la prenotazione.";
                });
                return;
            }

            bookingController.sendEmail(booking);
            bookingController.clearBookingFromSession();
            info("Prenotazione registrata! Pagherai in sede.");
            goHome();
            return;
        }

        // Flusso ONLINE: carta obbligatoria, totale = currentTotal (già aggiornato dal coupon)
        PaymentBean pb = new PaymentBean();
        pb.setCardHolderName(safeText(cardHolderField));
        pb.setCardNumber(safeText(cardNumberField));
        pb.setExpiry(safeText(expiryField));
        pb.setCvv(safeText(cvvField));
        pb.setCouponCode(safeText(couponField));
        pb.setAmount(currentTotal);

        List<String> errs = pb.validate();
        if (!errs.isEmpty()) {
            showError(String.join("\n", errs));
            return;
        }

        // Anti overbooking
        var free = bookingController.listAvailableStartTimes(
                booking.getBarbiereId(), booking.getDay(), booking.getServizioId());
        if (free == null || free.stream().noneMatch(t -> t.equals(booking.getStartTime()))) {
            showError("Lo slot selezionato non è più disponibile. Torna indietro e scegli un altro orario.");
            return;
        }

        try {
            String result = bookingController.paga(
                    pb.getCardHolderName(), pb.getCardNumber(), pb.getExpiry(), pb.getCvv(), pb.getAmount());

            if (!"success".equalsIgnoreCase(result)) {
                showError(switch (result) {
                    case "error:card_declined" -> "Carta rifiutata.";
                    case "error:coupon_invalid" -> "Coupon non valido al pagamento.";
                    case "error:slot_taken" -> "Orario non più disponibile.";
                    default -> "Errore durante il pagamento.";
                });
                return;
            }

            // OK pagamento -> salvo appuntamento CONFIRMED
            booking.setOnline();
            booking.setPrezzoTotale(currentTotal);
            booking.setCouponCode(pb.getCouponCode());

            String res = bookingController.book(booking);
            if (!"success".equalsIgnoreCase(res)) {
                showError("Prenotazione non registrata: " + res);
                return;
            }

            // Marca coupon come usato (se c'è) e accredita punti (facoltativo)
            if (couponBean.getCouponCode() != null && !couponBean.getCouponCode().isBlank()) {
                couponController.markCouponUsed(couponBean.getCouponCode(), booking.getClienteId());
            }
            // eventuale logica di punti:
            // int points = couponController.computePointsToAward(currentTotal);
            // loyaltyController.addPoints(booking.getClienteId(), points);

            bookingController.sendEmail(booking);
            bookingController.clearBookingFromSession();

            info("Pagamento completato e prenotazione confermata!");
            goHome();

        } catch (Exception ex) {
            showError("Errore inatteso: " + ex.getMessage());
        }
    }

    /* ===================== NAV ===================== */
    @FXML
    private void onBack() {
        try {
            WindowManager.getInstance().switchScene("RiepilogoViewAlternative.fxml", "Riepilogo");
        } catch (IOException e) {
            showError("Impossibile tornare indietro.");
        }
    }

    private void goHome() {
        try {
            WindowManager.getInstance().switchScene("HomeViewAlternative.fxml", "Home");
        } catch (IOException e) {
            showError("Impossibile tornare alla Home.");
        }
    }

    /* ===================== UI helpers ===================== */
    private void refreshTotalLabel() {
        if (totalLabel != null) totalLabel.setText("€ " + currentTotal.toPlainString());
         // riepilogo prezzo base
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

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private static String safeText(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }
}
