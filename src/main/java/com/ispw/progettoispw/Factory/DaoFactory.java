package com.ispw.progettoispw.Factory;

import com.ispw.progettoispw.Dao.*;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.Enum.StorageOption;
import com.ispw.progettoispw.entity.*;

 public final class DaoFactory implements DaoFactoryInterface {

    private static StorageOption storage=StorageOption.FILE;

    // Singleton
    private static volatile DaoFactory INSTANCE;

    private  GenericDao<Cliente> clienteDao;
    private GenericDao<Barbiere> barbiereDao;
    private  ReadOnlyDao<Servizio> servizioDao ;

    private  GenericDao<Appuntamento> appuntamentoDao ;

    private  GenericDao<LoyaltyAccount> loyaltyAccountDao;
    private  GenericDao<PersonalCoupon> personalCouponDao ;
    private  ReadOnlyDao<PrizeOption>   prizeOptionDao;

    private DaoFactory() {
        // inizializza una sola volta in base a storage
        switch (storage) {
            case INMEMORY -> {
                clienteDao        = new ClienteDaoMemory();
                barbiereDao       = new BarbiereDaoMemory();
                servizioDao       = new ServizioDaoMemory();

                appuntamentoDao   = new AppuntamentoDaoMemory();

                loyaltyAccountDao = new LoyaltyAccountDaoMemory();
                personalCouponDao = new PersonalCouponDaoMemory();
                prizeOptionDao = new PrizeOptionDaoMemory();
            }
            case FILE ->{
                clienteDao        = new ClienteDaoFile();
                barbiereDao       = new BarbiereDaoFile();
                servizioDao       = new ServizioDaoFile();

                appuntamentoDao   = new AppuntamentoDaoFile();

                loyaltyAccountDao = new LoyaltyAccountDaoFile();
                personalCouponDao = new PersonalCouponDaoFile();
                prizeOptionDao = new PrizeOptionDaoFile();

            }
        }
    }

    public static DaoFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (DaoFactory.class) {
                if (INSTANCE == null) INSTANCE = new DaoFactory();
            }
        }
        return INSTANCE;
    }

    // getters: ora restituiscono sempre le stesse istanze
    @Override public GenericDao<Cliente> getClienteDao() { return clienteDao; }
    @Override public GenericDao<Barbiere> getBarbiereDao() { return barbiereDao; }
    @Override public ReadOnlyDao<Servizio> getServizioDao() { return servizioDao; }

    @Override public GenericDao<Appuntamento> getAppuntamentoDao() { return appuntamentoDao; }

    @Override public GenericDao<LoyaltyAccount> getLoyaltyAccountDao() { return loyaltyAccountDao; }
    @Override public GenericDao<PersonalCoupon> getPersonalCouponDao() { return personalCouponDao; }
     @Override public ReadOnlyDao<PrizeOption>  getPrizeOptionDao() {return prizeOptionDao;}

    public  static void setStorageOption(StorageOption option) {
        if (option != null && INSTANCE == null) {
            storage = option; // deve essere impostato PRIMA della prima getInstance()
        }
    }
}
