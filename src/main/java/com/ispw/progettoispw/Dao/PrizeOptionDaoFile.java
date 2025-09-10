package com.ispw.progettoispw.Dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.PrizeOption;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class PrizeOptionDaoFile implements ReadOnlyDao<PrizeOption> {

    private static final String FILE_PATH = "prize_options.json";

    private final Map<String, PrizeOption> byId = new LinkedHashMap<>();
    private final Gson gson;

    public PrizeOptionDaoFile() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadFromFile();

        // se il file era vuoto → inizializza i premi di default
        if (byId.isEmpty()) {
            initDefaults();
            saveToFile();
        }
    }

    /* ===================== CRUD ===================== */

    @Override
    public PrizeOption read(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        return byId.get(id);
    }

    public PrizeOption read(String id) {
        return id == null ? null : byId.get(id);
    }

    public List<PrizeOption> readAll() {
        return List.copyOf(byId.values());
    }

    public void upsert(PrizeOption p) {
        if (p == null || p.getId() == null || p.getId().isBlank())
            throw new IllegalArgumentException("PrizeOption invalida");
        byId.put(p.getId(), p);
        saveToFile();
    }

    /* ===================== File I/O ===================== */

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<PrizeOption>>(){}.getType();
            List<PrizeOption> list = gson.fromJson(reader, listType);
            if (list != null) {
                byId.clear();
                for (PrizeOption p : list) {
                    if (p != null && p.getId() != null) {
                        byId.put(p.getId(), p);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(byId.values(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ===================== Inizializzazione ===================== */

    /** Premi iniziali di default */
    private void initDefaults() {
        byId.put("P1", new PrizeOption("P1", "Sconto Bronze", 10, new BigDecimal("5.00")));
        byId.put("P2", new PrizeOption("P2", "Sconto Silver", 20, new BigDecimal("12.00")));
        byId.put("P3", new PrizeOption("P3", "Sconto Gold",   30, new BigDecimal("20.00")));
    }
}
