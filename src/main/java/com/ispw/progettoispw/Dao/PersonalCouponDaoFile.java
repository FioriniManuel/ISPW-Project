package com.ispw.progettoispw.Dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.Enum.CouponStatus;
import com.ispw.progettoispw.entity.PersonalCoupon;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * DAO file-based per PersonalCoupon con indici:
 *  - byId:          couponId -> coupon
 *  - idsByClient:   clientId -> set di couponId
 *  - idByCode:      code (unique, case-sensitive) -> couponId
 *  - idsByStatus:   status -> set di couponId
 *
 * Persistenza: JSON su personal_coupons.json
 */
public class PersonalCouponDaoFile implements GenericDao<PersonalCoupon> {

    private static final String FILE_PATH = "personal_coupons.json";

    /* ===== Indici in memoria ===== */
    private final Map<String, PersonalCoupon> byId = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> idsByClient = new ConcurrentHashMap<>();
    private final Map<String, String> idByCode = new ConcurrentHashMap<>();
    private final Map<CouponStatus, Set<String>> idsByStatus = new ConcurrentHashMap<>();

    private final Object lock = new Object();
    private final Gson gson;

    public PersonalCouponDaoFile() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    /* ===================== File I/O ===================== */

    private void loadFromFile() {
        synchronized (lock) {
            byId.clear();
            idsByClient.clear();
            idByCode.clear();
            idsByStatus.clear();

            File f = new File(FILE_PATH);
            if (!f.exists()) return;

            try (Reader r = new FileReader(f)) {
                Type listType = new TypeToken<List<PersonalCoupon>>() {
                }.getType();
                List<PersonalCoupon> list = gson.fromJson(r, listType);
                if (list != null) {
                    for (PersonalCoupon c : list) {
                        if (c == null) continue;
                        String id = c.getCouponId();
                        String client = c.getClientId();
                        String code = normCode(c.getCode());
                        if (id == null || id.isBlank() || client == null || client.isBlank() || code == null || code.isEmpty()) {
                            // ignoro record malformati
                            continue;
                        }
                        // byId
                        byId.put(id, c);
                        // indici
                        addToIndex(idsByClient, client, id);
                        idByCode.put(code, id);
                        addToIndex(idsByStatus, c.getStatus(), id);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToFile() {
        synchronized (lock) {
            List<PersonalCoupon> snapshot = new ArrayList<>(byId.values());
            try (Writer w = new FileWriter(FILE_PATH)) {
                gson.toJson(snapshot, w);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* ===================== Helpers indici ===================== */

    private static <K> void addToIndex(Map<K, Set<String>> map, K key, String id) {
        if (key == null || id == null) return;
        map.compute(key, (k, set) -> {
            if (set == null) set = new ConcurrentSkipListSet<>();
            set.add(id);
            return set;
        });
    }

    private static <K> void removeFromIndex(Map<K, Set<String>> map, K key, String id) {
        if (key == null || id == null) return;
        map.computeIfPresent(key, (k, set) -> {
            set.remove(id);
            return set.isEmpty() ? null : set;
        });
    }

    private static String normCode(String code) {
        return code == null ? null : code.trim();
    }

    /* ===================== CRUD ===================== */

    @Override
    public void create(PersonalCoupon c) {
        Objects.requireNonNull(c, "PersonalCoupon null");

        synchronized (lock) {
            if (c.getCouponId() == null || c.getCouponId().isBlank()) {
                c.setCouponId(UUID.randomUUID().toString());
            }
            if (c.getClientId() == null || c.getClientId().isBlank()) {
                throw new IllegalArgumentException("clientId obbligatorio");
            }
            String code = normCode(c.getCode());
            if (code == null || code.isEmpty()) {
                throw new IllegalArgumentException("code obbligatorio");
            }

            // unicità code
            String prev = idByCode.putIfAbsent(code, c.getCouponId());
            if (prev != null) {
                throw new IllegalStateException("Codice coupon già esistente: " + code);
            }
            // no conflitto id
            PersonalCoupon existed = byId.putIfAbsent(c.getCouponId(), c);
            if (existed != null) {
                // rollback code
                idByCode.remove(code, c.getCouponId());
                throw new IllegalArgumentException("Coupon già presente: " + c.getCouponId());
            }

            // indicizza
            addToIndex(idsByClient, c.getClientId(), c.getCouponId());
            addToIndex(idsByStatus, c.getStatus(), c.getCouponId());

            saveToFile();
        }
    }

    @Override
    public PersonalCoupon read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        synchronized (lock) {
            PersonalCoupon c = byId.get(id);
            if (c == null) throw new NoSuchElementException("Coupon non trovato: " + id);
            return c;
        }
    }

    @Override
    public void update(PersonalCoupon c) {
        if (c == null || c.getCouponId() == null || c.getCouponId().isBlank()) {
            throw new IllegalArgumentException("Coupon o id null");
        }
        synchronized (lock) {
            PersonalCoupon current = byId.get(c.getCouponId());
            if (current == null) throw new NoSuchElementException("Coupon inesistente: " + c.getCouponId());

            // cambio code?
            String oldCode = normCode(current.getCode());
            String newCode = normCode(c.getCode());
            if (!Objects.equals(oldCode, newCode)) {
                if (newCode == null || newCode.isEmpty()) {
                    throw new IllegalArgumentException("code non può essere vuoto");
                }
                String prev = idByCode.putIfAbsent(newCode, c.getCouponId());
                if (prev != null && !prev.equals(c.getCouponId())) {
                    throw new IllegalStateException("Codice coupon già in uso: " + newCode);
                }
                if (oldCode != null) idByCode.remove(oldCode, c.getCouponId());
            }

            // cambio clientId?
            if (!Objects.equals(current.getClientId(), c.getClientId())) {
                removeFromIndex(idsByClient, current.getClientId(), c.getCouponId());
                addToIndex(idsByClient, c.getClientId(), c.getCouponId());
            }

            // cambio status?
            if (!Objects.equals(current.getStatus(), c.getStatus())) {
                removeFromIndex(idsByStatus, current.getStatus(), c.getCouponId());
                addToIndex(idsByStatus, c.getStatus(), c.getCouponId());
            }

            byId.put(c.getCouponId(), c);
            saveToFile();
        }
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        synchronized (lock) {
            PersonalCoupon removed = byId.remove(id);
            if (removed != null) {
                // pulisci indici
                String code = normCode(removed.getCode());
                if (code != null) idByCode.remove(code, id);
                removeFromIndex(idsByClient, removed.getClientId(), id);
                removeFromIndex(idsByStatus, removed.getStatus(), id);
                saveToFile();
            }
        }
    }

    @Override
    public List<PersonalCoupon> readAll() {
        synchronized (lock) {
            return byId.values().stream()
                    .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toUnmodifiableList());
        }
    }


    /**
     * Tutti i coupon di un cliente.
     */
    public List<PersonalCoupon> findByClient(String clientId) {
        if (clientId == null) return List.of();
        synchronized (lock) {
            Set<String> ids = idsByClient.getOrDefault(clientId, Set.of());
            return ids.stream().map(byId::get).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    /**
     * Tutti i coupon ATTIVI di un cliente.
     */
    public List<PersonalCoupon> findActiveByClient(String clientId) {
        return findByClient(clientId).stream()
                .filter(c -> c.getStatus() == CouponStatus.ACTIVE)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Tutti i coupon per stato (ACTIVE/USED).
     */
    public List<PersonalCoupon> findByStatus(CouponStatus status) {
        synchronized (lock) {
            if (status == null) return readAll();
            Set<String> ids = idsByStatus.getOrDefault(status, Set.of());
            return ids.stream().map(byId::get).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toUnmodifiableList());
        }
    }

}
