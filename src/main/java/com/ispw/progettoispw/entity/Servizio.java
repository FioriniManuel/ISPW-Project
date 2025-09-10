package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.Enum.GenderCategory;

import java.math.BigDecimal;

public class Servizio {
    private String serviceId;
    private String name;
    private BigDecimal price;
    private GenderCategory category;
    private int duration;
    public Servizio(){}

    public Servizio(String serviceId, String name, BigDecimal price, GenderCategory category,int duration){
        this.serviceId=serviceId;
        this.name=name;
        this.price=price;
        this.category=category;
        this.duration=duration;
    }

    // GETTER / SETTER

    public String getServiceId(){
        return serviceId;
    }

    public int getDuration() {
        return duration;
    }

    public BigDecimal getPrice() {
        return price;
    }



    public String getName() {
        return name;
    }



    public GenderCategory getCategory() {
        return category;
    }


    // METODI UTILI

    public String getFormattedPrice(){
        return String.format("€%.2f",price);
    }
    public String servizioPrezzo(){
        return name + "-" + getFormattedPrice();
    }

    public void setServiceId(String id) {
        this.serviceId=id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
