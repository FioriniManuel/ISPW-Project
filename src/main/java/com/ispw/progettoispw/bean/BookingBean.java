package com.ispw.progettoispw.bean;

import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingBean {

    // --- Identificativi principali ---
    private String appointmentId;   // <-- NEW: id dell’appuntamento salvato
    private String clienteId;
    private String barbiereId;
    private String barbiereDisplay;   // es. "Mario Rossi"



    // --- Dati temporali ---
    private LocalDate day;
    private LocalTime startTime;
    private LocalTime endTime;

    // --- Servizio scelto (singolo) ---
    private String serviziIds;                  // tuo nome attuale (contiene un solo id)
    private String serviceName;                 // <-- NEW: comodo per UI
    private int durataTotaleMin;
    private BigDecimal prezzoTotale = BigDecimal.ZERO;

    // --- Meta ---
    private GenderCategory categoria;
    private PaymentChannel canale;
    private String CouponCode;
    // --- Stato appuntamento ---
    private AppointmentStatus status;           // <-- NEW: per UI / gestione

    // ---------------- Getters/Setters ----------------
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = trim(appointmentId); }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = trim(clienteId); }

    public String getBarbiereId() { return barbiereId; }
    public void setBarbiereId(String barbiereId) { this.barbiereId = trim(barbiereId); }

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getBarbiereDisplay() { return barbiereDisplay; }
    public void setBarbiereDisplay(String barbiereDisplay) { this.barbiereDisplay = barbiereDisplay; }

    public String getServizioId() { return serviziIds; }
    public void setServiziId(String ids) { this.serviziIds = trim(ids); } // contiene 1 id

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = trim(serviceName); }

    public PaymentChannel getCanale() { return canale; }
    public void setInsede() { canale = PaymentChannel.IN_SHOP; }
    public void setOnline() { canale = PaymentChannel.ONLINE; }

    public int getDurataTotaleMin() { return durataTotaleMin; }
    public void setDurataTotaleMin(int durataTotaleMin) { this.durataTotaleMin = durataTotaleMin; }

    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(BigDecimal prezzoTotale) {
        this.prezzoTotale = (prezzoTotale == null) ? BigDecimal.ZERO : prezzoTotale;
    }

    public String getCouponCode() {
        return CouponCode;
    }

    public void setCouponCode(String couponCode) {
        CouponCode = couponCode;
    }

    public GenderCategory getCategoria() { return categoria; }
    public void setCategoria(GenderCategory categoria) { this.categoria = categoria; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    // ---------------- Logica bean ----------------
    public void computeEndTime() {
        if (startTime != null && durataTotaleMin > 0) {
            this.endTime = startTime.plusMinutes(durataTotaleMin);
        }
    }

    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (isBlank(clienteId)) errs.add("Cliente non valido.");
        if (isBlank(barbiereId)) errs.add("Barbiere non selezionato.");
        if (day == null) errs.add("Data non selezionata.");
        if (durataTotaleMin <= 0) errs.add("Durata totale non valida.");
        if (startTime == null) {
            errs.add("Orario di inizio non selezionato.");
        } else {
            if (endTime == null && durataTotaleMin > 0) computeEndTime();
            if (endTime == null || !startTime.isBefore(endTime)) errs.add("Intervallo orario non valido.");
        }
        return errs;
    }

    // ---------------- Helpers ----------------
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String trim(String s) { return s == null ? null : s.trim(); }

    public void setCanale(PaymentChannel paymentChannel) {
        this.canale=paymentChannel;
    }
}
