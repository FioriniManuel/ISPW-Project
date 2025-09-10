package com.ispw.progettoispw.Dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.Barbiere;


import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BarbiereDaoFile implements GenericDao<Barbiere> {
    private static final String FILE_PATH = "barbiere.json";
    private final Gson gson;
    private List<Barbiere> barbiere;

    public BarbiereDaoFile() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        barbiere = loadFromFile();
    }

    private List<Barbiere> loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<Barbiere>>() {}.getType();
            List<Barbiere> loadedClients = gson.fromJson(reader, listType);
            return loadedClients != null ? loadedClients : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(barbiere, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Barbiere entity) {
        if (read(entity.getEmail()) != null) {
            throw new IllegalArgumentException("Client already exists: " + entity.getEmail());
        }
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }
        barbiere.add(entity);
        saveToFile();
    }

    @Override
    public Barbiere read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for reading Client.");
        }
        String email = (String) keys[0];

        return barbiere.stream()
                .filter(barbiere-> barbiere.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Barbiere entity) {
        for (int i = 0; i < barbiere.size(); i++) {
            if (barbiere.get(i).getEmail().equals(entity.getEmail())) {
                barbiere.set(i, entity);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("Barbiere not found: " + entity.getEmail());
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for deleting Client.");
        }
        String email = (String) keys[0];

        barbiere.removeIf(barbiere -> barbiere.getEmail().equals(email));
        saveToFile();
    }

    @Override
    public List<Barbiere> readAll() {
        return new ArrayList<>(barbiere);
    }
}

