package com.ispw.progettoispw.bean;
import java.math.BigDecimal;

public class ServizioBean {
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final int durationMin;

    public ServizioBean(String id, String name, BigDecimal price, int durationMin) {
        this.id = id;
        this.name = name;
        this.price = price == null ? BigDecimal.ZERO : price;
        this.durationMin = durationMin;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getDurationMin() { return durationMin; }

    @Override public String toString() { return name; }
}
