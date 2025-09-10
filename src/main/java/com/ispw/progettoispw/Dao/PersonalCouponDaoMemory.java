package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.PersonalCoupon;
import com.ispw.progettoispw.Enum.CouponStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * DAO in-memory per PersonalCoupon con indici:
 *  - byId:          couponId -> coupon
 *  - idsByClient:   clientId -> set di couponId
 *  - idByCode:      code (unique, case-sensitive) -> couponId
 *  - idsByStatus:   status -> set di couponId
 */
public class PersonalCouponDaoMemory implements GenericDao<PersonalCoupon> {

    /* ===== Indici ===== */
    private final ConcurrentMap<String, PersonalCoupon> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> idsByClient = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> idByCode = new ConcurrentHashMap<>();
    private final ConcurrentMap<CouponStatus, Set<String>> idsByStatus = new ConcurrentHashMap<>();

    /* ===== Helpers indici ===== */

    private static <K> void addToIndex(ConcurrentMap<K, Set<String>> map, K key, String id) {
        if (key == null || id == null) return;
        map.compute(key, (k, set) -> {
            if (set == null) set = new ConcurrentSkipListSet<>();
            set.add(id);
            return set;
        });
    }

    private static <K> void removeFromIndex(ConcurrentMap<K, Set<String>> map, K key, String id) {
        if (key == null || id == null) return;
        map.computeIfPresent(key, (k, set) -> {
            set.remove(id);
            return set.isEmpty() ? null : set;
        });
    }

    private static String normCode(String code) {
        return code == null ? null : code.trim();
    }

    private void index(PersonalCoupon c) {
        final String id = c.getCouponId();
        addToIndex(idsByClient, c.getClientId(), id);
        addToIndex(idsByStatus, c.getStatus(), id);
        String code = normCode(c.getCode());
        if (code != null && !code.isEmpty()) {
            idByCode.put(code, id);
        }
    }

    private void unindex(PersonalCoupon c) {
        final String id = c.getCouponId();
        removeFromIndex(idsByClient, c.getClientId(), id);
        removeFromIndex(idsByStatus, c.getStatus(), id);
        String code = normCode(c.getCode());
        if (code != null && !code.isEmpty()) {
            idByCode.remove(code, id);
        }
    }

    /* ===== CRUD ===== */

    @Override
    public void create(PersonalCoupon c) {
        Objects.requireNonNull(c, "PersonalCoupon null");

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
        // Enforce unicità code
        String prevId = idByCode.putIfAbsent(code, c.getCouponId());
        if (prevId != null) {
            throw new IllegalStateException("Codice coupon già esistente: " + code);
        }

        PersonalCoupon prev = byId.putIfAbsent(c.getCouponId(), c);
        if (prev != null) {
            // rollback code index
            idByCode.remove(code, c.getCouponId());
            throw new IllegalArgumentException("Coupon già presente: " + c.getCouponId());
        }
        // indicizza gli altri
        addToIndex(idsByClient, c.getClientId(), c.getCouponId());
        addToIndex(idsByStatus, c.getStatus(), c.getCouponId());
    }

    @Override
    public PersonalCoupon read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        PersonalCoupon c = byId.get(id);
        if (c == null) throw new NoSuchElementException("Coupon non trovato: " + id);
        return c;
    }

    @Override
    public void update(PersonalCoupon c) {
        if (c == null || c.getCouponId() == null || c.getCouponId().isBlank()) {
            throw new IllegalArgumentException("Coupon o id null");
        }
        PersonalCoupon current = byId.get(c.getCouponId());
        if (current == null) throw new NoSuchElementException("Coupon inesistente: " + c.getCouponId());

        // gestisci eventuale cambio CODE (mantenendo unicità)
        String oldCode = normCode(current.getCode());
        String newCode = normCode(c.getCode());
        if (!Objects.equals(oldCode, newCode)) {
            if (newCode == null || newCode.isEmpty()) {
                throw new IllegalArgumentException("code non può essere vuoto");
            }
            // riserva il nuovo code
            String prev = idByCode.putIfAbsent(newCode, c.getCouponId());
            if (prev != null && !prev.equals(c.getCouponId())) {
                throw new IllegalStateException("Codice coupon già in uso: " + newCode);
            }
            // libera il vecchio
            if (oldCode != null) idByCode.remove(oldCode, c.getCouponId());
        }

        // gestisci cambio clientId
        if (!Objects.equals(current.getClientId(), c.getClientId())) {
            removeFromIndex(idsByClient, current.getClientId(), c.getCouponId());
            addToIndex(idsByClient, c.getClientId(), c.getCouponId());
        }

        // gestisci cambio status
        if (!Objects.equals(current.getStatus(), c.getStatus())) {
            removeFromIndex(idsByStatus, current.getStatus(), c.getCouponId());
            addToIndex(idsByStatus, c.getStatus(), c.getCouponId());
        }

        byId.put(c.getCouponId(), c);
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        PersonalCoupon removed = byId.remove(id);
        if (removed != null) {
            unindex(removed);
        }
    }

    @Override
    public List<PersonalCoupon> readAll() {
        return byId.values().stream()
                .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toUnmodifiableList());
    }



    /** Tutti i coupon di un cliente. */
    public List<PersonalCoupon> findByClient(String clientId) {
        if (clientId == null) return List.of();
        Set<String> ids = idsByClient.getOrDefault(clientId, Set.of());
        return ids.stream().map(byId::get).filter(Objects::nonNull)
                .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toUnmodifiableList());
    }

    /** Tutti i coupon ATTIVI di un cliente. */
    public List<PersonalCoupon> findActiveByClient(String clientId) {
        return findByClient(clientId).stream()
                .filter(c -> c.getStatus() == CouponStatus.ACTIVE)
                .collect(Collectors.toUnmodifiableList());
    }

    /** Tutti i coupon per stato (ACTIVE/USED). */
    public List<PersonalCoupon> findByStatus(CouponStatus status) {
        if (status == null) return readAll();
        Set<String> ids = idsByStatus.getOrDefault(status, Set.of());
        return ids.stream().map(byId::get).filter(Objects::nonNull)
                .sorted(Comparator.comparing(PersonalCoupon::getCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toUnmodifiableList());
    }


}
