package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.LoyaltyAccount;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO in-memory per LoyaltyAccount.
 * Indici:
 *  - byId:        loyaltyAccountId -> LoyaltyAccount
 *  - idByClient:  clientId -> loyaltyAccountId   (unicità 1:1 per cliente)
 */
public class LoyaltyAccountDaoMemory implements GenericDao<LoyaltyAccount> {

    private final Map<String, LoyaltyAccount> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idByClient = new ConcurrentHashMap<>();

    /* ===================== CRUD ===================== */

    @Override
    public void create(LoyaltyAccount acc) {
        Objects.requireNonNull(acc, "LoyaltyAccount null");

        // assicura un id
        if (acc.getLoyaltyAccountId() == null || acc.getLoyaltyAccountId().isBlank()) {
            acc.setLoyaltyAccountId(UUID.randomUUID().toString());
        }
        final String id = acc.getLoyaltyAccountId();

        // vincolo: un clientId può avere un solo loyalty account
        String clientId = acc.getClientId();
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId obbligatorio per LoyaltyAccount");
        }

        // riserva il clientId
        String prevId = idByClient.putIfAbsent(clientId, id);
        if (prevId != null) {
            throw new IllegalStateException("Esiste già un LoyaltyAccount per clientId=" + clientId);
        }

        // inserisci per id
        LoyaltyAccount old = byId.putIfAbsent(id, acc);
        if (old != null) {
            // rollback del secondario
            idByClient.remove(clientId, id);
            throw new IllegalArgumentException("LoyaltyAccount già presente: id=" + id);
        }
    }

    @Override
    public LoyaltyAccount read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        LoyaltyAccount acc = findByClientId(id);
        if (acc == null) throw new NoSuchElementException("LoyaltyAccount non trovato: " + id);
        return acc;
    }

    @Override
    public void update(LoyaltyAccount acc) {
        if (acc == null) throw new IllegalArgumentException("LoyaltyAccount null");
        String id = acc.getLoyaltyAccountId();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id mancante");

        LoyaltyAccount current = byId.get(id);
        if (current == null) throw new NoSuchElementException("Inesistente: " + id);

        // se cambia il clientId, aggiorna indice secondario mantenendo l’unicità
        if (!Objects.equals(current.getClientId(), acc.getClientId())) {
            if (acc.getClientId() == null || acc.getClientId().isBlank()) {
                throw new IllegalArgumentException("clientId obbligatorio");
            }
            // libera il vecchio mapping
            idByClient.remove(current.getClientId(), id);
            // riserva il nuovo
            String prev = idByClient.putIfAbsent(acc.getClientId(), id);
            if (prev != null && !prev.equals(id)) {
                // ripristina il vecchio mapping in caso di collisione
                idByClient.put(current.getClientId(), id);
                throw new IllegalStateException("Esiste già un account per clientId=" + acc.getClientId());
            }
        }

        byId.put(id, acc);
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        LoyaltyAccount removed = byId.remove(id);
        if (removed != null) {
            idByClient.remove(removed.getClientId(), id);
        }
    }

    @Override
    public List<LoyaltyAccount> readAll() {
        return List.copyOf(byId.values());
    }

    /* ===================== Query/Utility extra utili ===================== */

    /** Torna l'account per un dato clientId oppure null se assente. */
    public LoyaltyAccount findByClientId(String clientId) {
        if (clientId == null) return null;
        String id = idByClient.get(clientId);
        return id == null ? null : byId.get(id);
    }

    /** True se esiste un account per quel clientId. */
    public boolean existsForClient(String clientId) {
        return clientId != null && idByClient.containsKey(clientId);
    }

    /**
     * Restituisce l'account per il client, creandolo se non esiste (id generato).
     * Utile in fase di registrazione cliente.
     */
    public LoyaltyAccount getOrCreateForClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId obbligatorio");
        }
        // path veloce
        LoyaltyAccount existing = findByClientId(clientId);
        if (existing != null) return existing;

        // crea nuovo
        LoyaltyAccount created = new LoyaltyAccount(UUID.randomUUID().toString(), clientId);
        // potrebbe fallire se un altro thread lo crea in parallelo: gestisci con try-catch
        try {
            create(created);
            return created;
        } catch (IllegalStateException e) {
            // qualcun altro l'ha creato nel frattempo: leggi e ritorna
            LoyaltyAccount now = findByClientId(clientId);
            if (now != null) return now;
            throw e;
        }
    }

    /** Aggiunge punti all'account (persistendo l'oggetto aggiornato). */
    public void addPoints(String loyaltyAccountId, int pts) {
        LoyaltyAccount acc = read(loyaltyAccountId);
        acc.addPoints(pts);
        byId.put(loyaltyAccountId, acc);
    }

    /**
     * Prova a scalare punti dall'account (persistendo l'oggetto aggiornato).
     * @return true se riuscito, false se saldo insufficiente o pts <= 0
     */
    public boolean redeemPoints(String loyaltyAccountId, int pts) {
        LoyaltyAccount acc = read(loyaltyAccountId);
        boolean ok = acc.redeemPoints(pts);
        if (ok) byId.put(loyaltyAccountId, acc);
        return ok;
    }

    /** Verifica l'invariante di consistenza dell'account. */
    public boolean isConsistent(String loyaltyAccountId) {
        return read(loyaltyAccountId).isConsistent();
    }
}
