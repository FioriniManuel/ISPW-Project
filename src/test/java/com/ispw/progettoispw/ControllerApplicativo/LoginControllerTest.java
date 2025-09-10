package com.ispw.progettoispw.ControllerApplicativo;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.LoginBean;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Cliente;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;
    private GenericDao<Cliente>  clienteDao;
    private GenericDao<Barbiere> barbiereDao;

    @BeforeEach
    void setUp() {
        // prendi i DAO reali (in-memory) dalla factory e puliscili
        DaoFactory f = DaoFactory.getInstance();
        clienteDao  = f.getClienteDao();
        barbiereDao = f.getBarbiereDao();

        clearDao(clienteDao);
        clearDao(barbiereDao);

        // seed: un cliente e un barbiere noti
        Cliente c = new Cliente();
        c.setId("C1");
        c.setEmail("cliente@test.it");
        c.setPassword("pwd");
        c.setFirstName("Mario");
        c.setLastName("Rossi");
        c.setphoneNumber("3331112222");
        clienteDao.create(c);

        Barbiere b = new Barbiere();
        b.setId("B1");
        b.setEmail("barbiere@test.it");
        b.setPassword("pwd");
        b.setFirstName("Luca");
        b.setLastName("Bianchi");
        b.setphoneNumber("3339998888");
        barbiereDao.create(b);

        controller = new LoginController();
    }

    @AfterEach
    void tearDown() {
        // chiudi la sessione tra un test e l’altro
        SessionManager.getInstance().logout();
    }

    private static <T> void clearDao(GenericDao<T> dao) {
        // rimuovi tutti gli oggetti presenti
        for (T t : List.copyOf(dao.readAll())) {
            try {
                var id = t.getClass().getMethod("getId").invoke(t);
                dao.delete(id);
            } catch (Exception ignore) {

            }
        }
    }

    @Test
    void loginCliente_success() {
        LoginBean bean = new LoginBean();
        bean.setEmail("cliente@test.it");
        bean.setPassword("pwd");
        bean.setUserType(Role.CLIENTE);

        String esito = controller.login(bean);
        assertEquals("success:cliente", esito);
        assertNotNull(SessionManager.getInstance().getCurrentSession(), "La sessione deve essere aperta");
        assertEquals("cliente@test.it", SessionManager.getInstance().getCurrentSession().getEmail());
        assertEquals(Role.CLIENTE, SessionManager.getInstance().getCurrentSession().getRole());
    }

    @Test
    void loginBarbiere_success() {
        LoginBean bean = new LoginBean();
        bean.setEmail("barbiere@test.it");
        bean.setPassword("pwd");
        bean.setUserType(Role.BARBIERE);

        String esito = controller.login(bean);
        assertEquals("success:barbiere", esito);
        assertNotNull(SessionManager.getInstance().getCurrentSession());
        assertEquals(Role.BARBIERE, SessionManager.getInstance().getCurrentSession().getRole());
    }

    @Test
    void login_wrongCredentials() {
        LoginBean bean = new LoginBean();
        bean.setEmail("cliente@test.it");
        bean.setPassword("sbagliata");
        bean.setUserType(Role.CLIENTE);

        String esito = controller.login(bean);
        assertEquals("error:wrong_credentials", esito);
        assertNull(SessionManager.getInstance().getCurrentSession(), "Nessuna sessione deve essere aperta");
    }

    @Test
    void login_notFound() {
        LoginBean bean = new LoginBean();
        bean.setEmail("nessuno@test.it");
        bean.setPassword("pwd");
        bean.setUserType(Role.CLIENTE);

        String esito = controller.login(bean);
        assertEquals("error:not_found", esito);
        assertNull(SessionManager.getInstance().getCurrentSession());
    }

    @Test
    void login_validationError_emptyFields() {
        LoginBean bean = new LoginBean(); // email e password mancanti
        bean.setUserType(Role.CLIENTE);

        String esito = controller.login(bean);
        assertEquals("error:validation", esito);
        assertNull(SessionManager.getInstance().getCurrentSession());
    }
}
