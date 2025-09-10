package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.PrizeOption;
import java.math.BigDecimal;
import java.util.*;
public class PrizeOptionDaoMemory implements ReadOnlyDao<PrizeOption> {

    private final Map<String, PrizeOption> byId = new LinkedHashMap<>();

        public PrizeOptionDaoMemory() {
            // default (puoi cambiare)
            byId.put("P1", new PrizeOption("P1", "Sconto Bronze", 10, new BigDecimal("5.00")));
            byId.put("P2", new PrizeOption("P2", "Sconto Silver", 20, new BigDecimal("12.00")));
            byId.put("P3", new PrizeOption("P3", "Sconto Gold",   30, new BigDecimal("20.00")));
        }

    @Override
    public PrizeOption read(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        return byId.get(id);}

    public List<PrizeOption> readAll() {
            return List.copyOf(byId.values());
        }

        public PrizeOption read(String id) {
            return id == null ? null : byId.get(id);
        }

        // per barbieri (admin) per aggiornare la config
        public void upsert(PrizeOption p) {
            if (p == null || p.getId() == null || p.getId().isBlank())
                throw new IllegalArgumentException("PrizeOption invalida");
            byId.put(p.getId(), p);
        }
    }


