package com.ispw.progettoispw.Dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.LoyaltyAccount;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO file-based per LoyaltyAccount.
 * File JSON: loyalty_accounts.json
 *
 * Indici in memoria:
 *  - byId:        loyaltyAccountId -> LoyaltyAccount
 *  - idByClient:  clientId -> loyaltyAccountId   (unicità 1:1 per cliente)
 *
 * Nota: Si persiste una lista di LoyaltyAccount;
 * gli indici sono ricostruiti da file all'avvio e mantenuti allineati a runtime.
 */
public class LoyaltyAccountDaoFile implements GenericDao<LoyaltyAccount> {

    private static final String FILE_PATH = "loyalty_accounts.json";

    // Stato in memoria
    private final Map<String, LoyaltyAccount> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idByClient = new ConcurrentHashMap<>();

    private final Object lock = new Object();
    private final Gson gson;

    public LoyaltyAccountDaoFile() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    /* ===================== File I/O ===================== */

    private void loadFromFile() {
        synchronized (lock) {
            byId.clear();
            idByClient.clear();

            File f = new File(FILE_PATH);
            if (!f.exists()) return;

            try (Reader r = new FileReader(f)) {
                Type listType = new TypeToken<List<LoyaltyAccount>>() {}.getType();
                List<LoyaltyAccount> list = gson.fromJson(r, listType);
                if (list != null) {
                    for (LoyaltyAccount acc : list) {
                        if (acc == null) continue;
                        String id = acc.getLoyaltyAccountId();
                        String clientId = acc.getClientId();
                        if (id == null || id.isBlank() || clientId == null || clientId.isBlank()) {
                            // ignoro record malformati
                            continue;
                        }
                        byId.put(id, acc);
                        // in caso di duplicati clientId nel file, mantiene l'ultimo
                        idByClient.put(clientId, id);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToFile() {
        synchronized (lock) {
            List<LoyaltyAccount> list = new ArrayList<>(byId.values());
            try (Writer w = new FileWriter(FILE_PATH)) {
                gson.toJson(list, w);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* ===================== CRUD ===================== */

    @Override
    public void create(LoyaltyAccount acc) {
        Objects.requireNonNull(acc, "LoyaltyAccount null");

        synchronized (lock) {
            if (acc.getLoyaltyAccountId() == null || acc.getLoyaltyAccountId().isBlank()) {
                acc.setLoyaltyAccountId(UUID.randomUUID().toString());
            }
            final String id = acc.getLoyaltyAccountId();

            String clientId = acc.getClientId();
            if (clientId == null || clientId.isBlank()) {
                throw new IllegalArgumentException("clientId obbligatorio per LoyaltyAccount");
            }
            if (idByClient.containsKey(clientId)) {
                throw new IllegalStateException("Esiste già un LoyaltyAccount per clientId=" + clientId);
            }
            if (byId.containsKey(id)) {
                throw new IllegalArgumentException("LoyaltyAccount già presente: id=" + id);
            }

            byId.put(id, acc);
            idByClient.put(clientId, id);
            saveToFile();
        }
    }

    @Override
    public LoyaltyAccount read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        synchronized (lock) {
            LoyaltyAccount acc = findByClientId(id);
            if (acc == null) throw new NoSuchElementException("LoyaltyAccount non trovato: " + id);
            return acc;
        }
    }

    @Override
    public void update(LoyaltyAccount acc) {
        if (acc == null) throw new IllegalArgumentException("LoyaltyAccount null");
        String id = acc.getLoyaltyAccountId();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id mancante");

        synchronized (lock) {
            LoyaltyAccount current = byId.get(id);
            if (current == null) throw new NoSuchElementException("Inesistente: " + id);

            // gestisci eventuale cambio clientId, mantenendo unicità 1:1
            String oldClient = current.getClientId();
            String newClient = acc.getClientId();
            if (newClient == null || newClient.isBlank()) {
                throw new IllegalArgumentException("clientId obbligatorio");
            }
            if (!Objects.equals(oldClient, newClient)) {
                // libera vecchio mapping
                idByClient.remove(oldClient, id);
                // riserva nuovo
                String prev = idByClient.putIfAbsent(newClient, id);
                if (prev != null && !prev.equals(id)) {
                    // ripristina vecchio e fallisci
                    idByClient.put(oldClient, id);
                    throw new IllegalStateException("Esiste già un account per clientId=" + newClient);
                }
            }

            byId.put(id, acc);
            saveToFile();
        }
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        synchronized (lock) {
            LoyaltyAccount removed = byId.remove(id);
            if (removed != null) {
                idByClient.remove(removed.getClientId(), id);
                saveToFile();
            }
        }
    }

    @Override
    public List<LoyaltyAccount> readAll() {
        synchronized (lock) {
            return new ArrayList<>(byId.values());
        }
    }

    /* ===================== Query/Utility extra ===================== */

    /** Torna l'account per un dato clientId oppure null. */
    public LoyaltyAccount findByClientId(String clientId) {
        if (clientId == null) return null;
        synchronized (lock) {
            String id = idByClient.get(clientId);
            return id == null ? null : byId.get(id);
        }
    }

    /** True se esiste un account per quel clientId. */
    public boolean existsForClient(String clientId) {
        if (clientId == null) return false;
        synchronized (lock) {
            return idByClient.containsKey(clientId);
        }
    }

    /**
     * Restituisce l'account per il client, creandolo se non esiste.
     * L'ID viene generato automaticamente.
     */
    public LoyaltyAccount getOrCreateForClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId obbligatorio");
        }
        synchronized (lock) {
            LoyaltyAccount existing = findByClientId(clientId);
            if (existing != null) return existing;

            LoyaltyAccount created = new LoyaltyAccount(UUID.randomUUID().toString(), clientId);
            // ri-usa create() per rispettare i vincoli e persistere
            create(created);
            return created;
        }
    }

    /** Aggiunge punti e persiste. */
    public void addPoints(String loyaltyAccountId, int pts) {
        if (pts == 0) return;
        synchronized (lock) {
            LoyaltyAccount acc = read(loyaltyAccountId);
            acc.addPoints(pts);
            byId.put(loyaltyAccountId, acc);
            saveToFile();
        }
    }


    public boolean redeemPoints(String loyaltyAccountId, int pts) {
        synchronized (lock) {
            LoyaltyAccount acc = read(loyaltyAccountId);
            boolean ok = acc.redeemPoints(pts);
            if (ok) {
                byId.put(loyaltyAccountId, acc);
                saveToFile();
            }
            return ok;
        }
    }

    /** Check di consistenza dell'account. */
    public boolean isConsistent(String loyaltyAccountId) {
        synchronized (lock) {
            return read(loyaltyAccountId).isConsistent();
        }
    }
}
