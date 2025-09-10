package com.ispw.progettoispw.bean;
import java.math.BigDecimal;

public class CouponBean {




        private String clienteId;
        private String couponCode;          // inserito in UI
        private BigDecimal baseTotal;       // totale prima dello sconto
        private BigDecimal discountApplied; // sconto trovato (se c’è)
        private BigDecimal totalToPay;      // totale finale (base - discount)

        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }

        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

        public BigDecimal getBaseTotal() { return baseTotal; }
        public void setBaseTotal(BigDecimal baseTotal) { this.baseTotal = baseTotal; }

        public BigDecimal getDiscountApplied() { return discountApplied; }
        public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }

        public BigDecimal getTotalToPay() { return totalToPay; }
        public void setTotalToPay(BigDecimal totalToPay) { this.totalToPay = totalToPay; }
    }


