package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Exceptions.WrongLoginCredentialsException;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Session.Session;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.LoginBean;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Cliente;
import com.ispw.progettoispw.entity.User;

import java.util.Objects;
import java.util.logging.Logger;

public class LoginController {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final GenericDao<Cliente> clienteDao;
    private final GenericDao<Barbiere> barbiereDao;

    public LoginController() {
        DaoFactory factory = DaoFactory.getInstance(); // ✅ stessa factory per tutta l’app
        this.clienteDao  = factory.getClienteDao();
        this.barbiereDao = factory.getBarbiereDao();
    }

    /**
     * Autentica l’utente.
     * @return "success:cliente" | "success:barbiere" | "error:validation" | "error:not_found" | "error:wrong_credentials"
     */
    public String login(LoginBean bean) {
        if (!basicValidate(bean)) return "error:validation";

        final String emailNorm = normalizeEmail(bean.getEmail());
        final String rawPassword = Objects.requireNonNullElse(bean.getPassword(), "");
        Barbiere b = findBarbiereByEmail(emailNorm);
        Cliente c = findClienteByEmail(emailNorm);

        if(bean.getUserType()==Role.BARBIERE){

            if (b != null && passwordMatches(b.getPassword(), rawPassword)) {
                openSession(b, Role.BARBIERE);
                return "success:barbiere";}
        }
        else {
            if (c != null && passwordMatches(c.getPassword(), rawPassword)) {
                openSession(c, Role.CLIENTE);
                return "success:cliente";}}


        // tipo non indicato: provo CLIENTE poi BARBIERE

        if
        // utente non trovato oppure password errata
         (c == null && b == null) return "error:not_found";
        return "error:wrong_credentials";
    }

    // -------------------- helpers --------------------

    private boolean basicValidate(LoginBean bean) {
        if (bean == null) return false;
        String email = bean.getEmail();
        String pwd   = bean.getPassword();
        if (email == null || email.isBlank()) return false;
        if (pwd == null || pwd.isBlank())     return false;
        return true;
    }

    private Cliente findClienteByEmail(String emailNorm) {
        return clienteDao.readAll().stream()
                .filter(u -> u.getEmail() != null && emailNorm.equals(normalizeEmail(u.getEmail())))
                .findFirst()
                .orElse(null);
    }

    private Barbiere findBarbiereByEmail(String emailNorm) {
        return barbiereDao.readAll().stream()
                .filter(u -> u.getEmail() != null && emailNorm.equals(normalizeEmail(u.getEmail())))
                .findFirst()
                .orElse(null);
    }

    private void openSession(User user, Role role) {

        Session session = new Session(
                user.getphoneNumber(),
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                role

        );

        SessionManager.getInstance().login(session);
        logger.info("Login OK: " + user.getEmail() + " (" + role + ")" + user.getId());

    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean passwordMatches(String stored, String rawInput) {
        // Se usi hashing, sostituisci qui:
        // return PasswordHasher.matches(rawInput, stored);
        return Objects.equals(stored, rawInput);
    }
    public static String getName(){ Session session=SessionManager.getInstance().getCurrentSession();
        if(session==null) return null;
    return session.getDisplayName();}

    public static String getId(){Session session=SessionManager.getInstance().getCurrentSession();
    if(session==null) return null;
    return session.getId();}

    public static synchronized void logOut(){
    SessionManager.getInstance().logout();}

    public String getEmail() {
        Session session=SessionManager.getInstance().getCurrentSession();
        if(session==null) return null;
        return session.getEmail();
    }
}

