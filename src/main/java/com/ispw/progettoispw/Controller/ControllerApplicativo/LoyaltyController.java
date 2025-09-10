package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.entity.LoyaltyAccount;

/**
 * Controller applicativo per la gestione della loyalty card (punti).
 */
public class LoyaltyController {

    private final GenericDao<LoyaltyAccount> loyaltyDao;

    public LoyaltyController() {
        DaoFactory factory = DaoFactory.getInstance();
        this.loyaltyDao = factory.getLoyaltyAccountDao();
    }

    /** Ritorna i punti attuali di un cliente. */
    public int getPoints(String clientId) {
        if (clientId == null) return 0;
        LoyaltyAccount acc = loyaltyDao.read(clientId);
        return (acc == null) ? 0 : acc.getPoints();
    }

    /** Aggiorna direttamente i punti (set). */
    public void setPoints(String clientId, int pts) {
        LoyaltyAccount acc = loyaltyDao.read(clientId);
        if (acc == null) {
            acc = new LoyaltyAccount(clientId, pts);
            loyaltyDao.create(acc);
        } else {
            acc.setPoints(pts);
            loyaltyDao.update(acc);
        }
    }

    /** Incrementa di una certa quantità. */
    public void addPoints(String clientId, int delta) {
        if (clientId == null || delta <= 0) return;
        LoyaltyAccount acc = loyaltyDao.read(clientId);
        if (acc == null) {
            acc = new LoyaltyAccount(clientId, delta);
            loyaltyDao.create(acc);
        } else {
            acc.setPoints(acc.getPoints() + delta);
            loyaltyDao.update(acc);
        }
    }

    /** Scala punti (senza andare sotto zero). */
    public void redeemPoints(String clientId, int delta) {
        if (clientId == null || delta <= 0) return;
        LoyaltyAccount acc = loyaltyDao.read(clientId);
        if (acc == null) return;
        int newPts = Math.max(0, acc.getPoints() - delta);
        acc.setPoints(newPts);
        loyaltyDao.update(acc);
    }
}

