package com.ispw.progettoispw.Controller.ControllerApplicativo;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Enum.CouponStatus;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.entity.LoyaltyAccount;
import com.ispw.progettoispw.entity.PersonalCoupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CouponController {




        private static final int EURO_PER_POINT = 10; // 1 punto ogni 10€

        private final GenericDao<PersonalCoupon> couponDao;
        private final GenericDao<LoyaltyAccount>  loyaltyDao; // opzionale (per update reale)

        public CouponController() {
            DaoFactory f = DaoFactory.getInstance();
            this.couponDao = f.getPersonalCouponDao(); // Assicurati che esista in DaoFactory
            this.loyaltyDao = f.getLoyaltyAccountDao(); // Idem (opzionale se vuoi solo calcolare)
        }

        /**
         * Applica il coupon (se valido) e ritorna il nuovo totale.
         * NON modifica persistenza (coupon rimane ACTIVE finché non confermi il pagamento).
         *
         * Ritorna il totale da mostrare nella label (base - sconto, minimo 0).
         */
        public BigDecimal previewTotalWithCoupon(CouponBean bean, BigDecimal base) {
            Objects.requireNonNull(bean, "PaymentBean null");


            BigDecimal discount = BigDecimal.ZERO;

            String code = trim(bean.getCouponCode());
            if (!code.isBlank()) {
                PersonalCoupon c = findActiveCouponByCodeForClient(code, bean.getClienteId());
                if (c != null) {
                    discount = c.getValue();
                    if (discount == null || discount.signum() <= 0) discount = BigDecimal.ZERO;
                    // cap per non scendere sotto zero
                    if (discount.compareTo(base) > 0) discount = base;
                }
            }

            BigDecimal total = base.subtract(discount);
            if (total.signum() < 0) total = BigDecimal.ZERO;

            bean.setDiscountApplied(discount);
            bean.setTotalToPay(total);

            return total;
        }

        /**
         * Calcola i punti da accreditare sul pagamento (floor(totale / 10)).
         * NON fa update reale del LoyaltyAccount (lo puoi fare dopo la conferma).
         */
        public int computePointsToAward(BigDecimal totalToPay) {
            BigDecimal tot = safeNonNegative(totalToPay).setScale(0, RoundingMode.DOWN);
            return tot.divide(BigDecimal.valueOf(EURO_PER_POINT), RoundingMode.DOWN).intValue();
        }

        /* ===================== (Opzionale) Update reale dopo conferma ===================== */

        /**
         * Marca il coupon come USED (se esiste ed è coerente) — opzionale.
         */
        public void markCouponUsed(String couponCode, String clienteId) {
            if (couponCode == null || couponCode.isBlank()) return;
            PersonalCoupon c = findActiveCouponByCodeForClient(couponCode.trim(), clienteId);
            if (c == null) return;
            c.setStatus(CouponStatus.USED);
            couponDao.update(c);
            // se hai un GenericDao per coupon:
            // f.getPersonalCouponDaoWritable().update(c);
            // in alternativa, se ReadOnly, lascia la logica nel controller del pagamento definitivo
        }

        /**
         * Accumula punti nel LoyaltyAccount del cliente — opzionale.
         */
        public void addPointsToLoyalty(String clienteId, int points) {
            if (clienteId == null || points <= 0 || loyaltyDao == null) return;
            LoyaltyAccount acc = loyaltyDao.read(clienteId); // supponiamo chiave = clientId
            if (acc != null) {
                acc.addPoints(points);
                loyaltyDao.update(acc);
            }
        }
    public String createRewardCoupon(String clientId, BigDecimal value, String note) {
        if (clientId == null || value == null) return null;

        String code = "REWARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PersonalCoupon c = new PersonalCoupon();
        c.setCouponId(UUID.randomUUID().toString());
        c.setClientId(clientId);
        c.setCode(code);
        c.setValue(value);
        c.setStatus(CouponStatus.ACTIVE);
        c.setNote(note);

        couponDao.create(c);
        return code;
    }


        /* ===================== Helpers ===================== */
        public boolean reactivateCoupon(String code, String clienteId) {
            if (code == null || code.isBlank() || clienteId == null) {
                return false;
            }

            // recupera il coupon dal DAO
            Optional<PersonalCoupon> opt = couponDao.readAll().stream()
                    .filter(c -> code.equalsIgnoreCase(c.getCode()))
                    .filter(c -> clienteId.equals(c.getClientId())) // vincolo al cliente
                    .findFirst();

            if (opt.isEmpty()) {
                return false; // coupon non trovato
            }

            PersonalCoupon c = opt.get();

            if (c.getStatus() == CouponStatus.USED) {
                c.setStatus(CouponStatus.ACTIVE);
                couponDao.update(c); // persiste la modifica
                return true;
            }

            return false; // non era in stato USED
        }
        private PersonalCoupon findActiveCouponByCodeForClient(String code, String clienteId) {
            if (couponDao == null) return null;

            // Se NON hai un metodo custom in DAO, puoi fare readAll + filtro:
            for (PersonalCoupon c : couponDao.readAll()) {
                if (c == null) continue;
                boolean codeMatch = code.equalsIgnoreCase(trim(c.getCode()));
                boolean isOwner   = clienteId == null || clienteId.equals(c.getClientId()); // se vuoi vincolarlo al cliente
                boolean active    = c.getStatus() == CouponStatus.ACTIVE;
                if (codeMatch && isOwner && active) {
                    return c;
                }
            }
            return null;
        }

        private static BigDecimal safeNonNegative(BigDecimal v) {
            if (v == null || v.signum() < 0) return BigDecimal.ZERO;
            return v;
        }

        private static String trim(String s) {
            return s == null ? "" : s.trim();
        }
    }


