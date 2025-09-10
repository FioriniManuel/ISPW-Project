package com.ispw.progettoispw.Dao;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.entity.Appuntamento;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DAO file-based per Appuntamento.
 * Salva su JSON (appuntamenti.json) con adapter per LocalDate/LocalTime/Instant.
 */
public class AppuntamentoDaoFile implements GenericDao<Appuntamento> {

    private static final String FILE_PATH = "appuntamenti.json";

    private final Gson gson;
    private final Object lock = new Object(); // per thread-safety basilare
    private List<Appuntamento> items;

    public AppuntamentoDaoFile() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .setPrettyPrinting()
                .create();
        this.items = loadFromFile();
    }

    /* =================== File I/O =================== */

    private List<Appuntamento> loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader r = new FileReader(file)) {
            Type listType = new TypeToken<List<Appuntamento>>() {}.getType();
            List<Appuntamento> loaded = gson.fromJson(r, listType);
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer w = new FileWriter(FILE_PATH)) {
            gson.toJson(items, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* =================== GenericDao =================== */

    @Override
    public void create(Appuntamento entity) {
        Objects.requireNonNull(entity, "Appuntamento nullo");
        synchronized (lock) {
            // evita duplicati per id
            if (entity.getId() == null || entity.getId().isBlank()) {
                // se serve, genera id qui (di solito lo fa Appuntamento.newWithId())
                throw new IllegalArgumentException("Id appuntamento mancante. Usa Appuntamento.newWithId().");
            }
            if (read(entity.getId()) != null) {
                throw new IllegalArgumentException("Appuntamento già esistente: " + entity.getId());
            }
            items.add(entity);
            saveToFile();
        }
    }

    @Override
    public Appuntamento read(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        synchronized (lock) {
            for (Appuntamento a : items) {
                if (id.equals(a.getId())) return a;
            }
        }
        return null;
    }

    @Override
    public void update(Appuntamento entity) {
        Objects.requireNonNull(entity, "Appuntamento nullo");
        String id = entity.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id mancante in update()");
        }
        synchronized (lock) {
            for (int i = 0; i < items.size(); i++) {
                if (id.equals(items.get(i).getId())) {
                    items.set(i, entity);
                    saveToFile();
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Appuntamento non trovato: " + id);
    }

    @Override
    public void delete(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        synchronized (lock) {
            items.removeIf(a -> id.equals(a.getId()));
            saveToFile();
        }
    }

    @Override
    public List<Appuntamento> readAll() {
        synchronized (lock) {
            return new ArrayList<>(items);
        }
    }

    /* =================== Funzioni utili aggiuntive =================== */

    /** Ritorna gli appuntamenti di un barbiere in un certo giorno, ordinati per orario. */
    public List<Appuntamento> listByBarberOnDay(String barberId, LocalDate day) {
        if (barberId == null || day == null) return List.of();
        synchronized (lock) {
            List<Appuntamento> out = new ArrayList<>();
            for (Appuntamento a : items) {
                if (barberId.equals(a.getBarberId()) && day.equals(a.getDate())) {
                    out.add(a);
                }
            }
            out.sort(Comparator.comparing(Appuntamento::getSlotIndex));
            return out;
        }
    }

    /** Ritorna tutti gli appuntamenti di un cliente (qualsiasi giorno), ordinati per data e ora. */
    public List<Appuntamento> listByClient(String clientId) {
        if (clientId == null) return List.of();
        synchronized (lock) {
            List<Appuntamento> out = new ArrayList<>();
            for (Appuntamento a : items) {
                if (clientId.equals(a.getClientId())) {
                    out.add(a);
                }
            }
            out.sort(Comparator.comparing(Appuntamento::getDate)
                    .thenComparing(Appuntamento::getSlotIndex));
            return out;
        }
    }

    /** Aggiorna lo stato di un appuntamento (ritorna true se aggiornato). */
    public boolean updateStatus(String appointmentId, AppointmentStatus newStatus) {
        if (appointmentId == null || newStatus == null) return false;
        synchronized (lock) {
            for (int i = 0; i < items.size(); i++) {
                Appuntamento a = items.get(i);
                if (appointmentId.equals(a.getId())) {
                    a.setStatus(newStatus);
                    items.set(i, a);
                    saveToFile();
                    return true;
                }
            }
        }
        return false;
    }

    /* =================== Adapters JSON per java.time =================== */

    /** LocalDate come "yyyy-MM-dd" */
    public static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;
        @Override public JsonElement serialize(LocalDate src, Type t, JsonSerializationContext c) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(F));
        }
        @Override public LocalDate deserialize(JsonElement json, Type t, JsonDeserializationContext c)
                throws JsonParseException {
            return json == null || json.isJsonNull() ? null : LocalDate.parse(json.getAsString(), F);
        }
    }

    /** LocalTime come "HH:mm:ss" */
    public static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_TIME;
        @Override public JsonElement serialize(LocalTime src, Type t, JsonSerializationContext c) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(F));
        }
        @Override public LocalTime deserialize(JsonElement json, Type t, JsonDeserializationContext c)
                throws JsonParseException {
            return json == null || json.isJsonNull() ? null : LocalTime.parse(json.getAsString(), F);
        }
    }

    /** Instant come ISO-8601 (es. 2025-09-06T12:34:56Z) */
    public static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override public JsonElement serialize(Instant src, Type t, JsonSerializationContext c) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }
        @Override public Instant deserialize(JsonElement json, Type t, JsonDeserializationContext c)
                throws JsonParseException {
            return json == null || json.isJsonNull() ? null : Instant.parse(json.getAsString());
        }
    }
}
