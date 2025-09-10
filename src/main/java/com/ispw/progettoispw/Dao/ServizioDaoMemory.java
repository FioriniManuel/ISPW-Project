package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.Servizio;
import com.ispw.progettoispw.Enum.GenderCategory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.ispw.progettoispw.Enum.GenderCategory.*;

/**
 * DAO in-memory per Servizio, senza interfaccia.
 * Espone direttamente i metodi di lettura.
 */
public class ServizioDaoMemory implements ReadOnlyDao<Servizio> {

    private final Map<String, Servizio> byId;

    public ServizioDaoMemory()  {
        Map<String, Servizio> seed = new LinkedHashMap<>();
        seed.put("TAGLIO_UOMO",  new Servizio("TAGLIO_UOMO",  "Taglio Uomo",  new BigDecimal("15.00"), UOMO,30));
        seed.put("BARBA",        new Servizio("BARBA",        "Barba",        new BigDecimal("8.00"), UOMO,30));
        seed.put("TAGLIO_DONNA", new Servizio("TAGLIO_DONNA", "Taglio Donna", new BigDecimal("20.00"), DONNA,30));
        seed.put("PIEGA",        new Servizio("PIEGA",        "Piega",        new BigDecimal("20.00"), DONNA,30));
        seed.put("TAGLIO+SHAMPOO+STYLING",      new Servizio("TAGLIO+SHAMPOO+STYLING",      "Taglio+Shampoo+Styling Uomo",      new BigDecimal("20.00"),  UOMO,60));
        seed.put("TAGLIO+SHAMPOO_UOMO", new Servizio("TAGLIO+SHAMPOO_UOMO","Taglio+Shampoo Uomo",new BigDecimal("18.00"),UOMO,60));
        seed.put("MODELLATURA BARBA", new Servizio("MODELLATURA BARBA","Modellatura Barba",new BigDecimal("13.00"),UOMO,30));
        seed.put("BARBA_SAGOMATA+PANNO_CALDO", new Servizio("BARBA_SAGOMATA+PANNO_CALDO","Barba Sagomata + Panno Caldo",new BigDecimal("15.00"),UOMO,30));
        seed.put("TRATTAMENTO ANTICADUTA/ANTIFORFORA", new Servizio("TRATTAMENTO ANTICADUTA/ANTIFORFORA","Trattamento Anticaduta/Antiforfora",new BigDecimal("15.00"),UOMO,60));
        seed.put("COLORE",  new Servizio("COLORE",  "Colore",  new BigDecimal("25.00"), UOMO,60));
        seed.put("TAGLIO+BARBA", new Servizio("TAGLIO+BARBA","Taglio+Barba",new BigDecimal("20.00"),UOMO,60));
        seed.put("TAGLIO+BARBA+SHAMPOO",new Servizio("TAGLIO+BARBA+SHAMPOO","Taglio+Barba+Shampoo",new BigDecimal("25.00"),UOMO,60));
        seed.put("TAGLIO+PIEGA",new Servizio("TAGLIO+PIEGA","Taglio+Piega",new BigDecimal("30.00"),DONNA,60));
        seed.put("FRANGIA", new Servizio("FRANGIA","Frangia",new BigDecimal("10.00"),DONNA,30));
        seed.put("COLORE_RICRESCITA",new Servizio("COLORE_RICRESCITA","Colore Ricrescita", new BigDecimal("30.00"),DONNA,60));
        seed.put("COLPI_DI_SOLE",new Servizio("COLPI_DI_SOLE","Colpi di Sole",new BigDecimal("50.00"),DONNA,60));
        seed.put("TRATTAMENTO_ANTICRESPO_ALLA_CHERATINA",new Servizio("TRATTAMENTO_ANTICRESPO_ALLA_CHERATINA","Trattamento Anticrespo Alla Cheratina",new BigDecimal("70.00"),DONNA,90));
        seed.put("MASCHERA_NUTRIENTE",new Servizio("MASCHERA_NUTRIENTE","Maschera Nutriente", new BigDecimal("10.00"),DONNA,30));
        seed.put("RACCOLTO_DA_CERIMONIA", new Servizio("RACCOLTO_DA_CERIMONIA","Raccolto da Cerimonia",new BigDecimal("45.00"),DONNA,60));
        seed.put("ACCONCIATURA_SPOSA", new Servizio("ACCONCIATURA_SPOSA","Acconciatura Sposa (prova inclusa)", new BigDecimal("150.00"),DONNA,120));
        this.byId = Collections.unmodifiableMap(seed);
    }

    /** Leggi un servizio per id. */
    public Servizio read(String id) {
        return id == null ? null : byId.get(id);
    }

    @Override
    public Servizio read(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        return byId.get(id);
    }

    /** Tutti i servizi disponibili. */
    public List<Servizio> readAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public void upsert(Servizio updated) {

    }

    /** Servizi per categoria (UOMO, DONNA). */
    public List<Servizio> findByCategoria(GenderCategory categoria) {
        if (categoria == null) return readAll();
        return byId.values().stream()
                .filter(s -> s.getCategory() == categoria)
                .collect(Collectors.toUnmodifiableList());
    }




}
