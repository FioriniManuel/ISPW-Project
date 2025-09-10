package com.ispw.progettoispw.Dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.Cliente;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClienteDaoFile implements GenericDao<Cliente> {
    private static final String FILE_PATH = "clients.json";
    private final Gson gson;
    private List<Cliente> clients;

    public ClienteDaoFile() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        clients = loadFromFile();
    }

    private List<Cliente> loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<Cliente>>() {}.getType();
            List<Cliente> loadedClients = gson.fromJson(reader, listType);
            return loadedClients != null ? loadedClients : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(clients, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Cliente entity) {
        if (read(entity.getEmail()) != null) {
            throw new IllegalArgumentException("Client already exists: " + entity.getEmail());
        }
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }

        clients.add(entity);
        saveToFile();
    }

    @Override
    public Cliente read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for reading Client.");
        }
        String email = (String) keys[0];

        return clients.stream()
                .filter(client -> client.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Cliente entity) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getEmail().equals(entity.getEmail())) {
                clients.set(i, entity);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("Client not found: " + entity.getEmail());
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for deleting Client.");
        }
        String email = (String) keys[0];

        clients.removeIf(client -> client.getEmail().equals(email));
        saveToFile();
    }

    @Override
    public List<Cliente> readAll() {
        return new ArrayList<>(clients);
    }
}
