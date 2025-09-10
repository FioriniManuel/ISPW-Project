package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Enum.StorageOption;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.bean.RegistrationBean;
import com.ispw.progettoispw.entity.*;

import java.util.logging.Logger;




public class RegistrazioneController {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final GenericDao<Cliente> clientDao;
    private final GenericDao<Barbiere> barbiereDao;
    private final GenericDao<LoyaltyAccount> loyaltyAccountDao;

    public RegistrazioneController() {
        // Ottenere i DAO utilizzando DaoFactory
        DaoFactory daoFactory = DaoFactory.getInstance();
        this.clientDao = daoFactory.getClienteDao();
        this.barbiereDao = daoFactory.getBarbiereDao();
        this.loyaltyAccountDao=daoFactory.getLoyaltyAccountDao();
    }
    /** Metodo principale: decide se registrare Cliente o Barbiere in base alla bean */
    public String register(RegistrationBean bean) {
        var errors = bean.validate();
        if (!errors.isEmpty()) {
            logger.warning("Errori di validazione");
            return "error:validation";
        }

        if (bean.isClient()) {
            return registerCliente(bean);
        } else if (bean.isBarber()) {
            return registerBarbiere(bean);
        } else {
            return "error:unknown_user_type";
        }
    }

    // ----------------- Registrazione specifica Cliente -----------------
    private String registerCliente(RegistrationBean bean) {
        if (emailExistsCliente(bean.getEmail()) || emailExistsBarbiere(bean.getEmail())) return "error:email_exists";
        if (phoneExistsCliente(bean.getPhoneNumber()) || phoneExistsBarbiere(bean.getPhoneNumber())) return "error:phone_exists";

        Cliente c = mapToCliente(bean);
        try {
            clientDao.create(c);
            LoyaltyAccount loyalty =new LoyaltyAccount();
            loyalty.setClientId(c.getId());
            loyaltyAccountDao.create(loyalty);

            logger.info("Cliente registrato con successo: " + c.getEmail());
            return "success";
        } catch (IllegalStateException dup) {
            return "error:conflict";
        } catch (Exception ex) {
            logger.severe("Errore inatteso registrando cliente: " + ex.getMessage());
            return "error:unexpected";
        }
    }

    // ----------------- Registrazione specifica Barbiere ----------------
    private String registerBarbiere(RegistrationBean bean) {
        if (emailExistsBarbiere(bean.getEmail()) || emailExistsCliente(bean.getEmail())) return "error:email_exists";
        if (phoneExistsBarbiere(bean.getPhoneNumber())|| phoneExistsCliente(bean.getPhoneNumber())) return "error:phone_exists";

        Barbiere b = mapToBarbiere(bean);
        try {
            barbiereDao.create(b);
            logger.info("Barbiere registrato con successo: " + b.getEmail());
            return "success";
        } catch (IllegalStateException dup) {
            return "error:conflict";
        } catch (Exception ex) {
            logger.severe("Errore inatteso registrando barbiere: " + ex.getMessage());
            return "error:unexpected";
        }
    }

    // ----------------- Mapper Bean -> Entità -----------------
    private Cliente mapToCliente(RegistrationBean b) {
        Cliente c = new Cliente();
        c.setFirstName(b.getFirstName());
        c.setLastName(b.getLastName());
        c.setEmail(b.getEmail());
        c.setphoneNumber(normalizePhone(b.getPhoneNumber()));
        c.setPassword(b.getPassword());
        return c;
    }

    private Barbiere mapToBarbiere(RegistrationBean b) {
        Barbiere bar = new Barbiere();
        bar.setFirstName(b.getFirstName());
        bar.setLastName(b.getLastName());
        bar.setEmail(b.getEmail());
        bar.setphoneNumber(normalizePhone(b.getPhoneNumber()));
        bar.setPassword(b.getPassword());
        bar.setSpecializzazione(b.getSpecializzazione());
        bar.setActive( true );
        return bar;
    }

    // ----------------- Controlli di unicità -----------------
    private boolean emailExistsCliente(String email) {
        if (email == null) return false;
        String norm = email.trim().toLowerCase();
        return clientDao.readAll().stream()
                .anyMatch(c -> c.getEmail() != null &&
                        norm.equals(c.getEmail().trim().toLowerCase()));
    }

    private boolean phoneExistsCliente(String phone) {
        String norm = normalizePhone(phone);
        return clientDao.readAll().stream()
                .anyMatch(c -> c.getphoneNumber() != null &&
                        norm.equals(normalizePhone(c.getphoneNumber())));
    }

    private boolean emailExistsBarbiere(String email) {
        if (email == null) return false;
        String norm = email.trim().toLowerCase();
        return barbiereDao.readAll().stream()
                .anyMatch(b -> b.getEmail() != null &&
                        norm.equals(b.getEmail().trim().toLowerCase()));
    }

    private boolean phoneExistsBarbiere(String phone) {
        String norm = normalizePhone(phone);
        return barbiereDao.readAll().stream()
                .anyMatch(b -> b.getphoneNumber() != null &&
                        norm.equals(normalizePhone(b.getphoneNumber())));
    }

    // ----------------- Utility -----------------
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[\\s\\-()]", "");
        if (p.startsWith("+39"))  p = p.substring(3);
        if (p.startsWith("0039")) p = p.substring(4);
        return p;
    }


}
