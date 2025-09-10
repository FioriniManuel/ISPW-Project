package com.ispw.progettoispw.Dao;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.Servizio;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.ispw.progettoispw.Enum.GenderCategory.DONNA;
import static com.ispw.progettoispw.Enum.GenderCategory.UOMO;

public class ServizioDaoFile implements ReadOnlyDao<Servizio> {

    private static final Path DATA_PATH = Paths.get(
            System.getProperty("user.home"),
            ".progettoispw", "data", "servizi.json"
    );

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private List<Servizio> cache;       // mantiene l'ordine
    private Map<String, Servizio> byId; // indice veloce per id

    public ServizioDaoFile() { /* no I/O */ }

    /* ============= Lazy load ============= */

    private void ensureLoaded() {
        lock.writeLock().lock();
        try {
            if (cache != null && byId != null) return;
            List<Servizio> loaded = safeLoadFromFile();
            cache = new ArrayList<>(loaded);
            rebuildIndex();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void rebuildIndex() {
        Map<String, Servizio> tmp = new LinkedHashMap<>();
        for (Servizio s : cache) {
            if (s == null) continue;
            String id = s.getServiceId();
            if (id != null && !id.isBlank()) tmp.put(id, s);
        }
        byId = tmp;
    }

    /* ============= Load + seed robusto ============= */

    private List<Servizio> safeLoadFromFile() {
        try {
            Files.createDirectories(DATA_PATH.getParent());

            if (Files.notExists(DATA_PATH) || Files.size(DATA_PATH) == 0) {
                List<Servizio> seed = getDefaultSeed();
                persistInternal(seed);
                return seed;
            }

            try (BufferedReader br = Files.newBufferedReader(DATA_PATH, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<Servizio>>() {}.getType();
                JsonElement root = JsonParser.parseReader(br);

                if (root == null || root.isJsonNull() || !root.isJsonArray()) {
                    List<Servizio> seed = getDefaultSeed();
                    persistInternal(seed);
                    return seed;
                }

                List<Servizio> list = gson.fromJson(root, listType);
                if (list == null || list.isEmpty()) {
                    List<Servizio> seed = getDefaultSeed();
                    persistInternal(seed);
                    return seed;
                }
                return list;
            }
        } catch (EOFException | JsonSyntaxException e) {
            System.err.println("WARN: servizi.json malformato: " + e.getMessage());
            List<Servizio> seed = getDefaultSeed();
            persistInternal(seed);
            return seed;
        } catch (IOException io) {
            System.err.println("WARN: lettura servizi.json: " + io.getMessage());
            List<Servizio> seed = getDefaultSeed();
            persistInternal(seed);
            return seed;
        }
    }

    /* ============= Persistenza ============= */

    private void persist() {
        lock.readLock().lock();
        try {
            List<Servizio> snapshot = (cache == null) ? List.of() : new ArrayList<>(cache);
            persistInternal(snapshot);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void persistInternal(List<Servizio> toSave) {
        try {
            Files.createDirectories(DATA_PATH.getParent());
            Path tmp = DATA_PATH.resolveSibling(DATA_PATH.getFileName() + ".tmp");

            try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                gson.toJson(toSave, bw);
            }
            Files.move(tmp, DATA_PATH,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException io) {
            System.err.println("ERROR: salvataggio servizi.json fallito: " + io.getMessage());
        }
    }

    /* ============= Seed (20 servizi) ============= */

    private List<Servizio> getDefaultSeed() {
        List<Servizio> seed = new ArrayList<>();
        seed.add(new Servizio("TAGLIO_UOMO",  "Taglio Uomo",  new BigDecimal("15.00"), UOMO, 30));
        seed.add(new Servizio("BARBA",        "Barba",        new BigDecimal("8.00"),  UOMO, 30));
        seed.add(new Servizio("TAGLIO_DONNA", "Taglio Donna", new BigDecimal("20.00"), DONNA,30));
        seed.add(new Servizio("PIEGA",        "Piega",        new BigDecimal("20.00"), DONNA,30));
        seed.add(new Servizio("TAGLIO+SHAMPOO+STYLING","Taglio+Shampoo+Styling Uomo",new BigDecimal("20.00"),UOMO,60));
        seed.add(new Servizio("TAGLIO+SHAMPOO_UOMO","Taglio+Shampoo Uomo",new BigDecimal("18.00"),UOMO,60));
        seed.add(new Servizio("MODELLATURA BARBA","Modellatura Barba",new BigDecimal("13.00"),UOMO,30));
        seed.add(new Servizio("BARBA_SAGOMATA+PANNO_CALDO","Barba Sagomata + Panno Caldo",new BigDecimal("15.00"),UOMO,30));
        seed.add(new Servizio("TRATTAMENTO ANTICADUTA/ANTIFORFORA","Trattamento Anticaduta/Antiforfora",new BigDecimal("15.00"),UOMO,60));
        seed.add(new Servizio("COLORE",  "Colore",  new BigDecimal("25.00"), UOMO, 60));
        seed.add(new Servizio("TAGLIO+BARBA","Taglio+Barba",new BigDecimal("20.00"),UOMO,60));
        seed.add(new Servizio("TAGLIO+BARBA+SHAMPOO","Taglio+Barba+Shampoo",new BigDecimal("25.00"),UOMO,60));
        seed.add(new Servizio("TAGLIO+PIEGA","Taglio+Piega",new BigDecimal("30.00"),DONNA,60));
        seed.add(new Servizio("FRANGIA","Frangia",new BigDecimal("10.00"),DONNA,30));
        seed.add(new Servizio("COLORE_RICRESCITA","Colore Ricrescita", new BigDecimal("30.00"),DONNA,60));
        seed.add(new Servizio("COLPI_DI_SOLE","Colpi di Sole",new BigDecimal("50.00"),DONNA,60));
        seed.add(new Servizio("TRATTAMENTO_ANTICRESPO_ALLA_CHERATINA","Trattamento Anticrespo Alla Cheratina",new BigDecimal("70.00"),DONNA,90));
        seed.add(new Servizio("MASCHERA_NUTRIENTE","Maschera Nutriente", new BigDecimal("10.00"),DONNA,30));
        seed.add(new Servizio("RACCOLTO_DA_CERIMONIA","Raccolto da Cerimonia",new BigDecimal("45.00"),DONNA,60));
        seed.add(new Servizio("ACCONCIATURA_SPOSA","Acconciatura Sposa (prova inclusa)", new BigDecimal("150.00"),DONNA,120));
        return seed;
    }

    /* ============= API ReadOnlyDao ============= */

    @Override
    public Servizio read(Object... keys) {
        ensureLoaded();
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String serviceId");
        }
        lock.readLock().lock();
        try {
            return byId.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Servizio> readAll() {
        ensureLoaded();
        lock.readLock().lock();
        try {
            // usa la cache per mantenere l'ordine
            return Collections.unmodifiableList(new ArrayList<>(cache));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void upsert(Servizio updated) {
        Objects.requireNonNull(updated, "Servizio null");
        ensureLoaded();

        lock.writeLock().lock();
        try {
            String id = updated.getServiceId();
            if (id == null || id.isBlank()) {
                id = generateId();
                updated.setServiceId(id);
            }
            Servizio old = byId.get(id);
            if (old == null) {
                cache.add(updated);
            } else {
                int idx = indexOfById(id);
                if (idx >= 0) cache.set(idx, updated);
            }
            byId.put(id, updated);
        } finally {
            lock.writeLock().unlock();
        }
        persist();
    }

    /* ============= Util ============= */

    private int indexOfById(String id) {
        for (int i = 0; i < cache.size(); i++) {
            Servizio s = cache.get(i);
            if (s != null && id.equals(s.getServiceId())) return i;
        }
        return -1;
    }

    private String generateId() {
        return "S" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }

    public void reload() {
        lock.writeLock().lock();
        try {
            cache = null;
            byId = null;
            ensureLoaded();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
