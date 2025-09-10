package com.ispw.progettoispw.entity;



import java.math.BigDecimal;
import java.util.Objects;

public class PrizeOption {
    private String id;            // es: "P1", "P2", "P3"
    private String name;          // es: "Sconto Bronze"
    private int requiredPoints;   // punti da scalare
    private BigDecimal couponValue; // valore coupon generato

    public PrizeOption() {}

    public PrizeOption(String id, String name, int requiredPoints, BigDecimal couponValue) {
        this.id = id;
        this.name = name;
        this.requiredPoints = requiredPoints;
        this.couponValue = couponValue;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getRequiredPoints() { return requiredPoints; }
    public BigDecimal getCouponValue() { return couponValue; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRequiredPoints(int requiredPoints) { this.requiredPoints = requiredPoints; }
    public void setCouponValue(BigDecimal couponValue) { this.couponValue = couponValue; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrizeOption that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
