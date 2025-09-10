package com.ispw.progettoispw.bean;


import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean per pagamento con carta + coupon.
 * Pensata per essere popolata dal controller grafico e passata a un PaymentServiceFacade.
 */
public class PaymentBean {

    private String cardHolderName;   // "Nome Intestatario"
    private String cardNumber;       // "Numeri della Carta" (solo cifre)
    private String expiry;           // "Scadenza" (accetta "MM/YY" o "MM-YY")
    private String cvv;              // "CVV" (3-4 cifre)
    private String couponCode;       // opzionale
    private BigDecimal amount;       // importo totale da pagare (al netto o lordo, a scelta del flow)

    /* ===================== Getters/Setters ===================== */

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = trimOrNull(cardHolderName);
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) {
        // tieni solo le cifre
        if (cardNumber == null) {
            this.cardNumber = null;
        } else {
            this.cardNumber = cardNumber.replaceAll("\\D+", "");
        }
    }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) {
        this.expiry = trimOrNull(expiry);
    }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) {
        this.cvv = cvv == null ? null : cvv.replaceAll("\\D+", ""); // solo cifre
    }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) {
        this.couponCode = trimOrNull(couponCode);
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

    /* ===================== Validation ===================== */

    /** Ritorna lista di errori. Vuota se tutto ok. */
    public List<String> validate() {
        List<String> errs = new ArrayList<>();

        // Nome intestatario
        if (isBlank(cardHolderName)) {
            errs.add("Inserisci il nome dell'intestatario.");
        }

        // Numero carta
        if (isBlank(cardNumber)) {
            errs.add("Inserisci il numero della carta.");
        } else {
            if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                errs.add("Numero carta non valido (la lunghezza deve essere 13-19 cifre).");
            }

        }

        // Scadenza
        if (isBlank(expiry)) {
            errs.add("Inserisci la scadenza (MM/YY).");
        } else {
            YearMonth ym = parseExpiry(expiry);
            if (ym == null) {
                errs.add("Formato scadenza non valido. Usa MM/YY.");
            } else if (ym.isBefore(YearMonth.now())) {
                errs.add("La carta è scaduta.");
            }
        }

        // CVV
        if (isBlank(cvv)) {
            errs.add("Inserisci il CVV.");
        } else if (cvv.length() < 3 || cvv.length() > 4) {
            errs.add("CVV non valido (3 o 4 cifre).");
        }

        // Importo
        if (amount == null || amount.signum() <= 0) {
            errs.add("Importo non valido.");
        }

        return errs;
    }

    /** Parsing della scadenza: accetta "MM/YY" o "MM-YY". */
    public static YearMonth parseExpiry(String input) {
        if (input == null) return null;
        String s = input.trim();
        // normalizza separatore
        s = s.replace('-', '/');
        String[] parts = s.split("/");
        if (parts.length != 2) return null;
        try {
            int mm = Integer.parseInt(parts[0]);
            int yy = Integer.parseInt(parts[1]);
            if (mm < 1 || mm > 12) return null;
            // Anni 2000-2099: converto "25" in "2025"
            int yyyy = 2000 + yy;
            return YearMonth.of(yyyy, mm);
        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /* ===================== Helpers ===================== */

    public String maskedCard() {
        if (isBlank(cardNumber)) return "****";
        int len = cardNumber.length();
        String last4 = cardNumber.substring(Math.max(0, len - 4));
        return "**** **** **** " + last4;
    }

    public Integer getExpiryMonthOrNull() {
        YearMonth ym = parseExpiry(expiry);
        return ym == null ? null : ym.getMonthValue();
    }

    public Integer getExpiryYearOrNull() {
        YearMonth ym = parseExpiry(expiry);
        return ym == null ? null : ym.getYear();
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String trimOrNull(String s) { return s == null ? null : s.trim(); }
}
