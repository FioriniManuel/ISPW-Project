package com.ispw.progettoispw.model.entity;

import com.ispw.progettoispw.Enum.CouponStatus;

import java.math.BigDecimal;

public class PersonalCoupon {
    private String CouponId;
    private String ClientId;
    private String Code;
    private BigDecimal value;
    private CouponStatus status;
    private String note;

    public PersonalCoupon() {
    }

    public PersonalCoupon(String couponId, String code, String clientId, BigDecimal value,String Note) {
        this.CouponId = couponId;
        this.Code = code;
        this.ClientId = clientId;
        this.value = value;
        this.status = CouponStatus.ACTIVE;
        this.note=Note;
    }

    // GETTER / SETTER


    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCouponId() {
        return CouponId;
    }

    public void setCouponId(String couponId) {
        CouponId = couponId;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    // UTILITY MINIME

    public boolean isActive() {
        return status == CouponStatus.ACTIVE;
    }

    public void markUsed() {
        this.status = CouponStatus.USED;

    }


    public void setNote(String note) {
        this.note=note;
    }
    public String getNote(){
        return note;
    }
}