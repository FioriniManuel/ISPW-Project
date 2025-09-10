package com.ispw.progettoispw.bean;

import java.math.BigDecimal;

public class PrizeBean {
    private String id;              // es. "P1"
    private String name;            // es. "Taglio + Shampoo"
    private int requiredPoints;     // punti necessari
    private BigDecimal couponValue; // valore coupon

    public PrizeBean() {}

    public PrizeBean(String id, String name, int requiredPoints, BigDecimal couponValue) {
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
}
