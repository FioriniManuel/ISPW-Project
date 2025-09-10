package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.ApplicationFacade.ApplicationFacade;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.Enum.PaymentChannel;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import com.ispw.progettoispw.entity.Appuntamento;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Servizio;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingController {

    /* Orari di lavoro */
    private static final LocalTime MORN_START = LocalTime.of(8, 0);
    private static final LocalTime MORN_END   = LocalTime.of(13, 0);
    private static final LocalTime AFT_START  = LocalTime.of(14, 0);
    private static final LocalTime AFT_END    = LocalTime.of(20, 0);
    private static final int STEP_MIN = 30;

    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    /* DAO */
    private final ReadOnlyDao<Servizio> servizioDao;
    private final GenericDao<Barbiere> barbiereDao;
    private final GenericDao<Appuntamento> appuntamentoDao;

    public BookingController() {
        DaoFactory factory = DaoFactory.getInstance();
        this.servizioDao     = factory.getServizioDao();
        this.barbiereDao     = factory.getBarbiereDao();
        this.appuntamentoDao = factory.getAppuntamentoDao();
    }

    /* ===================== SERVIZI ===================== */

    public List<Servizio> listServiziByCategory(GenderCategory cat) {
        List<Servizio> all = servizioDao.readAll();
        if (all == null) return List.of();
        if (cat == null) return all;
        List<Servizio> out = new ArrayList<>();
        for (Servizio s : all) {
            if (s != null && s.getCategory() == cat) out.add(s);
        }
        return out;
    }

    public String buildServiceLabel(Servizio s) {
        BigDecimal price = (s.getPrice() == null) ? BigDecimal.ZERO : s.getPrice();
        return s.getName() + "  [€ " + PRICE_FMT.format(price) + "]";
    }

    public Servizio getServizio(String id) { return id == null ? null : servizioDao.read(id); }




    /* ===================== BARBIERI DISPONIBILI ===================== */
    // Ritorna i barbieri filtrati SOLO per genere (opzionalmente solo attivi)
    public List<Barbiere> listBarbersByGender(GenderCategory cat) {
        if (cat == null) return List.of();
        return barbiereDao.readAll().stream()
                .filter(Barbiere::isActive)                  // solo attivi
                .filter(b -> b.getSpecializzazione() == cat) // stessa specializzazione
                .toList();
    }

    /** Variante completa: data + servizio (quindi categoria+durata). */
    public List<Barbiere> availableBarbers(LocalDate day, String servizioId) {
        if (day == null || servizioId == null || servizioId.isBlank()) return List.of();

        Servizio s = servizioDao.read(servizioId);
        if (s == null) return List.of();

        GenderCategory cat = s.getCategory();
        int durata = s.getDuration();
        if (durata <= 0) return List.of();

        List<Barbiere> candidates = barbiereDao.readAll().stream()
                .filter(Barbiere::isActive)
                .filter(b -> b.getSpecializzazione() == cat) // enum === enum
                .toList();

        List<Barbiere> out = new ArrayList<>();
        for (Barbiere b : candidates) {
            if (!listAvailableStartTimesWithoutService(b.getId(), day).isEmpty()) {
                out.add(b);
            }
        }
        return out;
    }
    // IMPORT:
// import com.ispw.progettoispw.bean.BarbiereVM;
// (più gli import già presenti)
    // ... nel tuo BookingController

    // ====== MAPPER ENTITY → VM (privati) ======
    private ServizioBean toVM(Servizio s) {
        if (s == null) return null;
        return new ServizioBean(s.getServiceId(), s.getName(), s.getPrice(), s.getDuration());
    }


// ====== METODI PUBBLICI CHE RITORNANO VM ======

    // servizi per categoria → VM
    public java.util.List<ServizioBean> listServiziByCategoryVM(GenderCategory cat) {
        return listServiziByCategory(cat).stream()
                .map(this::toVM)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // etichetta servizio da VM (se vuoi evitare entity in GUI)
    public String buildServiceLabel(ServizioBean s) {
        java.math.BigDecimal price = (s.getPrice() == null) ? java.math.BigDecimal.ZERO : s.getPrice();
        return s.getName() + "  [€ " + PRICE_FMT.format(price) + "]";
    }

    // barbieri per genere → VM
    public java.util.List<BarbiereBean> listBarbersByGenderVM(GenderCategory cat) {
        return listBarbersByGender(cat).stream()
                .map(this::toVM)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // servizio (entity) → VM (per arricchire la BookingBean quando serve)
    public ServizioBean getServizioVM(String id) {
        Servizio s = getServizio(id);
        return (s == null) ? null : toVM(s);
    }

    private BarbiereBean toVM(com.ispw.progettoispw.entity.Barbiere b) {
        if (b == null) return null;
        String fn = b.getFirstName() == null ? "" : b.getFirstName();
        String ln = b.getLastName()  == null ? "" : b.getLastName();
        String full = (fn + " " + ln).trim();
        if (full.isEmpty()) full = (b.getEmail() != null && !b.getEmail().isBlank()) ? b.getEmail() : b.getId();
        return new BarbiereBean(b.getId(), full, b.getEmail());
    }

    /** Come availableBarbers(...) ma ritorna VM per la GUI */
    public java.util.List<BarbiereBean> availableBarbersVM(java.time.LocalDate day, String servizioId) {
        return availableBarbers(day, servizioId).stream()
                .map(this::toVM)
                .toList();
    }




    /* ===================== ORARI DISPONIBILI ===================== */

    /** Orari di inizio disponibili per barbiere/giorno in base alla durata del servizio. */
    public List<LocalTime> listAvailableStartTimes(String barbiereId, LocalDate day, String servizioId) {
        if (barbiereId == null || day == null || servizioId == null) return List.of();

        Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null) return List.of();

        int durata = servizio.getDuration();
        if (durata <= 0) return List.of();

        List<Appuntamento> booked = appuntamentoDao.readAll().stream()
                .filter(a -> barbiereId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<LocalTime> candidates = new ArrayList<>();
        candidates.addAll(generateStarts(MORN_START, MORN_END, durata));
        candidates.addAll(generateStarts(AFT_START, AFT_END, durata));

        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t : candidates) {
            if (isFreeInterval(t, t.plusMinutes(durata), booked)) out.add(t);
        }
        return out;
    }

    /** Orari di inizio disponibili senza servizio (slot minimo STEP_MIN). */
    private List<LocalTime> listAvailableStartTimesWithoutService(String barbiereId, LocalDate day) {
        if (barbiereId == null || day == null) return List.of();

        List<Appuntamento> booked = appuntamentoDao.readAll().stream()
                .filter(a -> barbiereId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<LocalTime> candidates = new ArrayList<>();
        candidates.addAll(generateStarts(MORN_START, MORN_END, STEP_MIN));
        candidates.addAll(generateStarts(AFT_START, AFT_END, STEP_MIN));

        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t : candidates) {
            if (isFreeInterval(t, t.plusMinutes(STEP_MIN), booked)) out.add(t);
        }
        return out;
    }

    private List<LocalTime> generateStarts(LocalTime from, LocalTime to, int durataMin) {
        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t = from; !t.plusMinutes(durataMin).isAfter(to); t = t.plusMinutes(STEP_MIN)) {
            out.add(t);
        }
        return out;
    }

    private boolean isFreeInterval(LocalTime startT, LocalTime endT, List<Appuntamento> existing) {
        for (Appuntamento a : existing) {
            if (overlap(startT, endT, a.getSlotIndex(), a.getSlotFin())) return false;
        }
        return true;
    }

    private boolean overlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    /* ===================== BOOK ===================== */

    /** Crea appuntamento PENDING (il salvataggio “vero” lo fai quando vuoi confermare). */
    public String book(BookingBean bean) {
        if (bean == null) return "error:validation";

        String servizioId = bean.getServizioId();
        Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null) return "error:validation";

        bean.setDurataTotaleMin(servizio.getDuration());
        bean.computeEndTime();

        List<String> errs = bean.validate();
        if (!errs.isEmpty()) return "error:validation";

        List<LocalTime> free = listAvailableStartTimes(bean.getBarbiereId(), bean.getDay(), servizioId);
        if (free.stream().noneMatch(t -> t.equals(bean.getStartTime()))) {
            return "error:slot_taken";
        }

        Appuntamento a = Appuntamento.newWithId();
        a.setServizio(bean.getServizioId());
        a.setClientId(bean.getClienteId());
        a.setBarberId(bean.getBarbiereId());
        a.setDate(bean.getDay());
        a.setSlotIndex(bean.getStartTime());
        a.setSlotFin(bean.getEndTime());
        a.setBaseAmount(bean.getPrezzoTotale());
        a.setAppliedCouponCode(bean.getCouponCode());

            if(bean.getCanale()== PaymentChannel.IN_SHOP){
                a.setInsede();
        a.setStatus(AppointmentStatus.PENDING);}
            else{ a.setStatus(AppointmentStatus.CONFIRMED);
            a.setOnline();}

        appuntamentoDao.create(a);

        return "success";
    }
    public String bookOnline(BookingBean bean) {
     if (bean == null) return "error:validation";

    String servizioId = bean.getServizioId();
    Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null) return "error:validation";

        bean.setDurataTotaleMin(servizio.getDuration());
        bean.computeEndTime();

    List<String> errs = bean.validate();
        if (!errs.isEmpty()) return "error:validation";

    List<LocalTime> free = listAvailableStartTimes(bean.getBarbiereId(), bean.getDay(), servizioId);
        if (free.stream().noneMatch(t -> t.equals(bean.getStartTime()))) {
        return "error:slot_taken";
    }
        return "success";}



    public void saveBookingToSession(BookingBean bean) {
        SessionManager.getInstance().setCurrentBooking(bean);
    }

    /** Recupera la BookingBean dalla sessione. */
    public BookingBean getBookingFromSession() {
        return SessionManager.getInstance().getCurrentBooking();
    }

    /** Pulisce la BookingBean dalla sessione (da chiamare a fine flusso). */
    public void clearBookingFromSession() {
        SessionManager.getInstance().clearCurrentBooking();
    }

    // Ritorna la lista “view” per la UI del cliente usando BookingBean
    public List<BookingBean> listCustomerAppointmentsVM(String clientId) {
        if (clientId == null) return List.of();

        return appuntamentoDao.readAll().stream()
                .filter(a -> clientId.equals(a.getClientId()))
                .sorted(Comparator.comparing(Appuntamento::getDate)
                        .thenComparing(Appuntamento::getSlotIndex))
                .map(a -> {
                    BookingBean b = new BookingBean();
                    b.setAppointmentId(a.getId());
                    b.setClienteId(a.getClientId());
                    b.setBarbiereId(a.getBarberId());
                    b.setDay(a.getDate());
                    b.setStartTime(a.getSlotIndex());
                    b.setEndTime(a.getSlotFin());
                    b.setPrezzoTotale(a.getBaseAmount());
                    b.setStatus(a.getStatus());
                    b.setCouponCode(a.getAppliedCouponCode());
                    b.setCanale(a.getPaymentChannel());

                // servizio
                    b.setServiziId(a.getServizio());
                    var s = servizioDao.read(a.getServizio());
                    if (s != null) {
                        b.setServiceName(s.getName());
                        b.setCategoria(s.getCategory());
                        b.setDurataTotaleMin(s.getDuration());
                    }
                    return b;
                })
                .toList();
    }
    // ===================== BEAN VIEW MODEL ==================
    public List<BookingBean> listAppointmentsForBarberOnDayVM(String barberId, LocalDate day) {
        if (barberId == null || day == null) return List.of();

        // prendi le entity dal DAO
        List<Appuntamento> entityList = appuntamentoDao.readAll().stream()
                .filter(a -> barberId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<BookingBean> beans = new ArrayList<>();
        for (Appuntamento a : entityList) {
            BookingBean b = new BookingBean();
            b.setClienteId(a.getClientId());
            b.setBarbiereId(a.getBarberId());
            b.setDay(a.getDate());
            b.setStartTime(a.getSlotIndex());
            b.setEndTime(a.getSlotFin());
            b.setDurataTotaleMin(
                    (int) java.time.Duration.between(a.getSlotIndex(), a.getSlotFin()).toMinutes()
            );
            b.setPrezzoTotale(a.getBaseAmount());

            // metto anche l'id appuntamento per update rapido
            b.setAppointmentId(a.getId());
            b.setStatus(a.getStatus());

            // arricchisco con info servizio
            Servizio s = servizioDao.read(a.getServizio());
            if (s != null) {
                b.setServiziId(s.getServiceId());
                b.setCategoria(s.getCategory());
                b.setPrezzoTotale(s.getPrice());
                b.setDurataTotaleMin(s.getDuration());
                b.setServiceName(s.getName()); // metodo aggiuntivo in BookingBean
            }

            beans.add(b);
        }
        return beans;
    }


    public boolean updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus) {
        if (appointmentId == null || newStatus == null) return false;

        Appuntamento a = appuntamentoDao.read(appointmentId);
        if (a == null) return false;

        a.setStatus(newStatus);
        appuntamentoDao.update(a);
        return true;
    }

    // Cancella un appuntamento per id (ritorna true se aggiornato)
    public boolean cancelCustomerAppointment(String appointmentId) {
        if (appointmentId == null) return false;
        Appuntamento a = appuntamentoDao.read(appointmentId);
        if (a == null) return false;
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            return false; // già “finale”
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        appuntamentoDao.update(a);
        return true;
    }


    public void sendEmail(BookingBean bean) {
        ApplicationFacade.sendBookingEmail(bean);
    }

    public String paga(String intestatario, String cardNumber, String expire, String cvv, BigDecimal amount) {

        return ApplicationFacade.processPayment(intestatario,cardNumber, expire, cvv, amount);

    }

}
