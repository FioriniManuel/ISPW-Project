package com.ispw.progettoispw.Session;

import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.BookingBean;

import java.util.Optional;

public final class SessionManager {
    private BookingBean currentBooking;


    // ---------- Singleton ----------
    private static volatile SessionManager instance;
    private SessionManager() {}
    public static SessionManager getInstance() {
        SessionManager ref = instance;
        if (ref == null) {
            synchronized (SessionManager.class) {
                ref = instance;
                if (ref == null) {
                    ref = instance = new SessionManager();
                }
            }
        }
        return ref;
    }

    // ---------- Stato ----------
    private Session currentSession; // null se non loggato

    // ---------- API ----------
    /** Da chiamare SOLO dopo aver validato credenziali a monte (DAO/Service). */
    public synchronized void login(Session session) {
        this.currentSession = session;
    }

    /** Svuota la sessione corrente. */
    public synchronized void logout() {
        this.currentSession = null;
    }

    public boolean isAuthenticated() {
        return currentSession != null;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(currentSession).map(Session::getRole);
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(currentSession).map(Session::getId);
    }

    public Optional<String> getUserEmail() {
        return Optional.ofNullable(currentSession).map(Session::getEmail);
    }

    public Optional<String> getUserDisplayName() {
        return Optional.ofNullable(currentSession).map(Session::getDisplayName);
    }

    /** Comodo per schermate riservate a un ruolo specifico. */
    public void requireRole(Role required) {
        if (!isAuthenticated() || currentSession.getRole() != required) {
            throw new IllegalStateException("Accesso negato: richiesto ruolo " + required);
        }
    }

public BookingBean getCurrentBooking() { return currentBooking; }
public void setCurrentBooking(BookingBean currentBooking) { this.currentBooking = currentBooking; }
public void clearCurrentBooking() { this.currentBooking = null; }

/* ====== Pulizia totale ====== */

}



