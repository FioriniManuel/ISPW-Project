package com.ispw.progettoispw.Factory;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.entity.*;

/**
 * Porta unica per ottenere i DAO dell’applicazione.
 * Oggi può essere implementata da InMemoryDaoFactory,
 * domani da DbDaoFactory senza toccare i service.
 */
public interface DaoFactoryInterface {

    GenericDao<Cliente>        getClienteDao();
    GenericDao<Barbiere>       getBarbiereDao();
    ReadOnlyDao<Servizio> getServizioDao();

    GenericDao<Appuntamento>   getAppuntamentoDao();

    GenericDao<LoyaltyAccount> getLoyaltyAccountDao();
    GenericDao<PersonalCoupon> getPersonalCouponDao();
    ReadOnlyDao<PrizeOption>   getPrizeOptionDao();
}
