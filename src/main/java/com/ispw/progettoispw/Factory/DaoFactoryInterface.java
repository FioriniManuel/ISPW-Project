package com.ispw.progettoispw.Factory;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.entity.Appuntamento;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Cliente;
import com.ispw.progettoispw.entity.LoyaltyAccount;
import com.ispw.progettoispw.entity.Ordine;
import com.ispw.progettoispw.entity.PersonalCoupon;
import com.ispw.progettoispw.entity.Prodotto;
import com.ispw.progettoispw.entity.Servizio;

/**
 * Porta unica per ottenere i DAO dell’applicazione.
 * Oggi può essere implementata da InMemoryDaoFactory,
 * domani da DbDaoFactory senza toccare i service.
 */
public interface DaoFactory {

    GenericDao<Cliente>        clienteDao();
    GenericDao<Barbiere>       barbiereDao();
    ReadOnlyDao<Servizio> servizioDao();
    GenericDao<Prodotto>       prodottoDao();
    GenericDao<Appuntamento>   appuntamentoDao();
    GenericDao<Ordine>         ordineDao();
    GenericDao<LoyaltyAccount> loyaltyAccountDao();
    GenericDao<PersonalCoupon> personalCouponDao();
}
