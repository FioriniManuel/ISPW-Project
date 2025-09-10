package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ClienteDaoMemoryTest {

    private ClienteDaoMemory dao;

    @BeforeEach
    void setUp() {
        dao = new ClienteDaoMemory();
    }

    private Cliente nuovoCliente(String id, String email, String phone) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setEmail(email);
        c.setphoneNumber(phone); // getter nel DAO è getphoneNumber()
        c.setFirstName("Mario");
        c.setLastName("Rossi");
        return c;
    }

    @Test
    void createAndReadById_ok() {
        Cliente c = nuovoCliente("ID1", "mario@example.com", "3331112222");
        dao.create(c);

        Cliente got = dao.read("ID1");
        assertNotNull(got);
        assertEquals("mario@example.com", got.getEmail());
        assertEquals("3331112222", got.getphoneNumber());
    }

    @Test
    void create_withoutId_generatesId() {
        Cliente c = nuovoCliente(null, "anna@example.com", "3339998888");
        dao.create(c);

        assertNotNull(c.getId(), "L'id deve essere generato");
        Cliente got = dao.read(c.getId());
        assertEquals("anna@example.com", got.getEmail());
    }

    @Test
    void create_duplicateEmail_throws() {
        dao.create(nuovoCliente("C1", "dup@example.com", "3201111111"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                dao.create(nuovoCliente("C2", "dup@example.com", "3202222222"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void create_duplicatePhone_throws() {
        dao.create(nuovoCliente("C1", "a1@example.com", "3200000000"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                dao.create(nuovoCliente("C2", "a2@example.com", "3200000000"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("telefono"));
    }

    @Test
    void read_wrongKeyTypeOrArity_throws() {
        assertThrows(IllegalArgumentException.class, () -> dao.read());
        assertThrows(IllegalArgumentException.class, () -> dao.read(123)); // non String
        assertThrows(IllegalArgumentException.class, () -> dao.read("A", "B")); // troppe chiavi
    }

    @Test
    void read_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> dao.read("UNKNOWN_ID"));
    }



    @Test
    void readAll_returnsSnapshot() {
        dao.create(nuovoCliente("A", "a@example.com", "3000000001"));
        dao.create(nuovoCliente("B", "b@example.com", "3000000002"));

        var list = dao.readAll();
        assertEquals(2, list.size());
        // mutare la lista ritornata non deve toccare lo storage interno
        list.clear();
        assertEquals(2, dao.readAll().size());
    }
}
