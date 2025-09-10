package com.ispw.progettoispw.model.entity;

import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Enum.CouponStatus;
import com.ispw.progettoispw.Enum.PaymentChannel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Appuntamento con pagamento "leggero":
 * - Sconto SOLO tramite coupon personale (nessun consumo punti in checkout)
 * - Accreditamento punti al pagamento
 * - Rimborso: riattiva il coupon e storna i punti accreditati
 */
public class Appuntamento {

    /* ===== Identità e riferimenti ===== */
    private String id;
    private String clientId;     // può essere null per ospite
    private String barberId;

    /* ===== Data/slot ===== */
    private LocalDate date;
    private LocalTime slotInit;
    private  LocalTime slotFin;// es. slot da 20 minuti

    /* ===== Stato ===== */
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /* ===== Importi ===== */
    private BigDecimal baseAmount = BigDecimal.ZERO;     // somma servizi (snapshot al momento della prenotazione)
    private BigDecimal discountAmount = BigDecimal.ZERO; // SOLO da coupon
    private BigDecimal total = BigDecimal.ZERO;          // base - discount (>=0)

    /* ===== Coupon (snapshot) ===== */
    private String appliedCouponId;      // PersonalCoupon.couponId
    private String appliedCouponCode;    // codice mostrato all'utente

    /* ===== Loyalty (snapshot) ===== */
    private Integer loyaltyPointsEarned; // punti accreditati al pagamento

    /* ===== Pagamento ===== */
    private PaymentChannel paymentChannel; // ONLINE / IN_SHOP
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant paidAt;
    private Instant refundedAt;
    private String servizio;

    /* ===== Costruttori / factory ===== */
    public Appuntamento() {}

    public static Appuntamento newWithId() {
        Appuntamento a = new Appuntamento();
        a.id = UUID.randomUUID().toString();
        return a;
    }

    public Appuntamento(String clientId, String barberId, LocalDate date, LocalTime slotInit,LocalTime slotFin ,BigDecimal baseAmount) {
        this.id = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.barberId = Objects.requireNonNull(barberId, "barberId");
        this.date = Objects.requireNonNull(date, "date");
        this.slotFin = slotFin;
        this.slotInit=slotInit;
        setBaseAmount(baseAmount);
        recomputeTotal();
    }

    /* ===== Getter/Setter essenziali ===== */
    public String getId() { return id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; touch(); }

    public String getBarberId() { return barberId; }
    public void setBarberId(String barberId) { this.barberId = barberId; touch(); }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; touch(); }

    public LocalTime getSlotIndex() { return slotInit; }
    public void setSlotIndex(LocalTime slotInit) { this.slotInit= slotInit; touch(); }
    public LocalTime getSlotFin(){ return slotFin;}

    public void setSlotFin(LocalTime slotFin) {
        this.slotFin = slotFin;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; touch(); }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.signum() < 0) throw new IllegalArgumentException("baseAmount >= 0");
        this.baseAmount = baseAmount;
        recomputeTotal();
    }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotal() { return total; }

    public String getAppliedCouponId() { return appliedCouponId; }
    public String getAppliedCouponCode() { return appliedCouponCode; }

    public Integer getLoyaltyPointsEarned() { return loyaltyPointsEarned; }

    public PaymentChannel getPaymentChannel() { return paymentChannel; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getRefundedAt() { return refundedAt; }

    /* ===== Sconto: SOLO coupon ===== */

    /**
     * Applica un coupon personale all'appuntamento.
     * Non cambia lo stato del coupon qui; verrà marcato USED solo in markPaid.
     * @return true se applicato, false se non valido
     */
    public boolean applyPersonalCoupon(PersonalCoupon coupon, String currentClientId) {
        if (coupon == null) return false;
        if (coupon.getStatus() != CouponStatus.ACTIVE) return false;
        if (coupon.getValue() == null || coupon.getValue().signum() <= 0) return false;
        if (coupon.getClientId() == null || !coupon.getClientId().equals(currentClientId)) return false;

        this.appliedCouponId = coupon.getCouponId();
        this.appliedCouponCode = coupon.getCode();

        BigDecimal newDiscount = nonNegative(this.discountAmount.add(coupon.getValue()));
        if (newDiscount.compareTo(baseAmount) > 0) newDiscount = baseAmount; // cap per non andare sotto zero
        this.discountAmount = newDiscount;

        recomputeTotal();
        return true;
    }

    /* ===== Pagamento e rimborso ===== */

    /**
     * Pagamento riuscito:
     * - setta canale e paidAt
     * - accredita punti (addPoints) sul totale pagato
     * - marca il coupon come USED (se era quello applicato)
     * - porta lo stato a CONFIRMED (potrai mettere COMPLETED a servizio eseguito)
     */
    public void markPaid(PaymentChannel channel, LoyaltyAccount loyaltyAccount, int euroPerPointEarnFloor,
                         PersonalCoupon couponIfAny) {

        this.paymentChannel = channel;
        this.paidAt = Instant.now();

        // accredito punti (se previsto)
        if (loyaltyAccount != null && euroPerPointEarnFloor > 0) {
            int eurosFloor = total.setScale(0, RoundingMode.DOWN).intValue();
            int earned = eurosFloor / euroPerPointEarnFloor;
            if (earned > 0) {
                loyaltyAccount.addPoints(earned);
                this.loyaltyPointsEarned = (this.loyaltyPointsEarned == null ? 0 : this.loyaltyPointsEarned) + earned;
            }
        }

        // marca coupon USED se coerente
        if (couponIfAny != null && this.appliedCouponId != null
                && this.appliedCouponId.equals(couponIfAny.getCouponId())
                && couponIfAny.getStatus() == CouponStatus.ACTIVE) {
            couponIfAny.markUsed();
        }

        if (this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.CONFIRMED;
        }
        touch();
    }

    /** Annulla prima del pagamento. */
    public void cancel() {
        if (this.paidAt != null) {
            throw new IllegalStateException("Già pagato: usa refundFull");
        }
        this.status = AppointmentStatus.CANCELLED;
        touch();
    }

    /**
     * Rimborso totale:
     * - riattiva il coupon (ACTIVE) se era stato usato su questo appuntamento
     * - storna i punti accreditati con questo pagamento (redeemPoints)
     * - imposta CANCELLED per coerenza con la tua FSM
     */
    public void refundFull(LoyaltyAccount loyaltyAccount, PersonalCoupon couponIfAny) {
        if (this.paidAt == null) {
            throw new IllegalStateException("Rimborsabile solo se già pagato");
        }
        this.refundedAt = Instant.now();


        // riattiva coupon se era quello usato
        if (couponIfAny != null && this.appliedCouponId != null
                && this.appliedCouponId.equals(couponIfAny.getCouponId())
                && couponIfAny.getStatus() == CouponStatus.USED) {
            couponIfAny.setStatus(CouponStatus.ACTIVE);
        }

        // storna punti accreditati
        if (loyaltyAccount != null) {
            int earned = this.loyaltyPointsEarned == null ? 0 : this.loyaltyPointsEarned;
            if (earned > 0) {
                loyaltyAccount.redeemPoints(earned); // toglie i punti dati per questo pagamento
                this.loyaltyPointsEarned = 0;
            }
        }

        this.status = AppointmentStatus.CANCELLED;
        touch();
    }

    /* ===== Totali e helper ===== */
    public void recomputeTotal() {
        BigDecimal d = (discountAmount == null) ? BigDecimal.ZERO : discountAmount;
        total = baseAmount.subtract(d);
        if (total.signum() < 0) total = BigDecimal.ZERO;
        touch();
    }

    private BigDecimal nonNegative(BigDecimal v) { return (v == null || v.signum() < 0) ? BigDecimal.ZERO : v; }
    private void touch() { this.updatedAt = Instant.now(); }

    public void setId(String id) {
        this.id = id;
    }

    public String getServizio() {
         return servizio;
    }

    public void setServizio(String servizioId) {
        this.servizio=servizioId;
    }
}
