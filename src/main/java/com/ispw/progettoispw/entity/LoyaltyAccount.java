package com.ispw.progettoispw.model.entity;

public class LoyaltyAccount {
    private String LoyaltyAccountId;
    private String clientId;
    private int currentPoints;
    private int lifetimeEarned;
    private int lifetimeRedeemed;

    public LoyaltyAccount(){}

    public LoyaltyAccount(String LoyaltyAccountId,String clientId){
        this.LoyaltyAccountId=LoyaltyAccountId;
        this.clientId=clientId;
        this.currentPoints=0;
        this.lifetimeEarned=0;
        this.lifetimeRedeemed=0;
    }
    public LoyaltyAccount(String clientId,int pts){
        this.clientId=clientId;
        this.currentPoints=pts;
    }
    public LoyaltyAccount(String LoyaltyAccountId,String clientId, int currentPoints, int lifetimeEarned, int lifetimeRedeemed){
        this.LoyaltyAccountId=LoyaltyAccountId;
        this.clientId=clientId;
        this.currentPoints= Math.max(0,currentPoints);
        this.lifetimeEarned=Math.max(0,lifetimeEarned);
        this.lifetimeRedeemed=Math.max(0,lifetimeRedeemed);
    }

    // GETTER / SETTER


    public String getLoyaltyAccountId() {
        return LoyaltyAccountId;
    }

    public void setLoyaltyAccountId(String loyaltyAccountId) {
        LoyaltyAccountId = loyaltyAccountId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getPoints() {
        return currentPoints;
    }

    public void setPoints(int currentPoints) {
        this.currentPoints = Math.max(0,currentPoints);
    }

    public int getLifetimeEarned() {
        return lifetimeEarned;
    }

    public void setLifetimeEarned(int lifetimeEarned) {
        this.lifetimeEarned = Math.max(0,lifetimeEarned);
    }

    public int getLifetimeRedeemed() {
        return lifetimeRedeemed;
    }

    public void setLifetimeRedeemed(int lifetimeRedeemed) {
        this.lifetimeRedeemed = Math.max(0,lifetimeRedeemed);
    }

    // METODI UTILI

    public void addPoints(int pts){
        if( pts <= 0 ){ return;}
            this.currentPoints+=pts;
            this.lifetimeEarned+=pts;}

    public boolean redeemPoints(int pts){
        if(pts <=0 || pts > this.currentPoints){ return false;}
        this.currentPoints -= pts;
        this.lifetimeRedeemed += pts;
        return true;

    }

    public int getRemainingPoints(){
        return this.currentPoints;
    }

    public boolean isConsistent(){
        return this.currentPoints == (this.lifetimeEarned - this.lifetimeRedeemed);
    }
}
