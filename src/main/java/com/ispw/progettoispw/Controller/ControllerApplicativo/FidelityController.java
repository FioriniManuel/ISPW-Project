package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.bean.PrizeBean;
import com.ispw.progettoispw.entity.PrizeOption;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

// Dipendenze "di alto livello" già esistenti nel tuo progetto:


public class FidelityController {

    private final ReadOnlyDao<PrizeOption> prizeDao; // configurazione premi (modificabile dai barbieri)
    private final LoyaltyController loyaltyController;  // getPoints / redeemPoints
    private final CouponController couponController;    // createRewardCoupon

    public FidelityController() {
        DaoFactory factory = DaoFactory.getInstance(); // usa la tua factory singleton
        this.prizeDao = factory.getPrizeOptionDao();   // <-- aggiungi questo getter nella tua DaoFactory
        this.loyaltyController = new LoyaltyController();
        this.couponController  = new CouponController();
    }

    /** Premi configurati (per UI). */
    public List<PrizeOption> listPrizes() {
        return prizeDao.readAll();
    }
    public PrizeOption getPrizeOption(String id) {
        if (id == null || id.isBlank()) return null;
        return prizeDao.read(id);
    }
    // dentro FidelityController
    public List<PrizeBean> listPrizesVM() {
        return listPrizes()  // <-- tuo metodo attuale che usa le entity
                .stream()
                .map(p -> new PrizeBean(p.getId(), p.getName(), p.getRequiredPoints(), p.getCouponValue()))
                .toList();
    }


    public void updatePrizeOption(String id, String name, int requiredPoints, BigDecimal couponValue) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID premio mancante");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome premio mancante");
        if (requiredPoints <= 0)
            throw new IllegalArgumentException("I punti richiesti devono essere > 0");
        if (couponValue == null || couponValue.signum() < 0)
            throw new IllegalArgumentException("Il valore coupon deve essere >= 0");

        PrizeOption p = new PrizeOption(id, name, requiredPoints, couponValue);
        prizeDao.upsert(p);
    }

    /** Punti correnti del cliente. */
    public int getCustomerPoints(String clientId) {
        if (clientId == null || clientId.isBlank()) return 0;
        return loyaltyController.getPoints(clientId); // delega al tuo controller/dao
    }

    /** Verifica se cliente può riscattare il premio. */
    public boolean canRedeem(String clientId, String prizeId) {
        PrizeOption p = prizeDao.read(prizeId);
        if (p == null) return false;
        int pts = getCustomerPoints(clientId);
        return pts >= p.getRequiredPoints();
    }

    /**
     * Esegue il riscatto:
     * - scala punti necessari
     * - crea un coupon personale del valore del premio
     * @return codice coupon generato
     */
    public String redeem(String clientId, String prizeId) {
        Objects.requireNonNull(clientId, "clientId");
        PrizeOption p = prizeDao.read(prizeId);
        if (p == null) throw new NoSuchElementException("Premio inesistente: " + prizeId);

        int pts = getCustomerPoints(clientId);
        if (pts < p.getRequiredPoints()) {
            throw new IllegalStateException("Punti insufficienti per il premio selezionato.");
        }

        // 1) scala punti
        loyaltyController.redeemPoints(clientId, p.getRequiredPoints());

        // 2) crea coupon
        BigDecimal value = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        String code = couponController.createRewardCoupon(clientId, value, "Premio fidelity: " + p.getName());
        return code;
    }

    /* ============ operazioni per barbieri (update premi) ============ */

    public void updatePrize(PrizeOption updated) {
        if (updated == null) throw new IllegalArgumentException("Premio nullo");
        if (updated.getId() == null || updated.getId().isBlank())
            throw new IllegalArgumentException("Id premio mancante");
        prizeDao.upsert(updated);
    }
}
