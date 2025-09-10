package com.ispw.progettoispw.ApplicationFacade;
import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Servizio;
import com.ispw.progettoispw.Session.SessionManager; // <-- adatta il package della tua Session!

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
public class ApplicationFacade {



    /**
     * Facade minimale: un solo metodo pubblico per inviare l'email di conferma
     * a partire dalla BookingBean (salvata in sessione).
     */


        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
        private static final DecimalFormat     PRICE_FMT = new DecimalFormat("#,##0.00");


        /**
         * Invia l'email di conferma prenotazione.
         * - Usa SOLO la BookingBean passata.
         * - Ricava email del cliente dalla Sessione.
         * - Risolve nome servizio/prezzo e (se possibile) nome barbiere.
         */
        public static void sendBookingEmail(BookingBean bean) {
            Objects.requireNonNull(bean, "BookingBean nulla");

            // 1) Email del cliente dalla sessione (adatta il getter in base al tuo SessionManager)
            String toEmail = SessionManager.getInstance().getCurrentSession().getEmail(); // es: getEmail() / getUserEmail()
            if (toEmail == null || toEmail.isBlank()) {
                // fallback: non mandiamo nulla se manca l'email
                return;
            }

            // 2) Risolviamo dettagli “parlanti”
            BookingController bookingController = new BookingController();

            // Servizio
            String servizioName = "-";
            String prezzoStr    = "€ 0,00";
            Servizio s = bookingController.getServizio(bean.getServizioId());
            if (s != null) {
                servizioName = safe(s.getName());
                if (s.getPrice() != null) {
                    prezzoStr = "€ " + PRICE_FMT.format(s.getPrice());
                }
            }

            // Barbiere (risolto da DAO a partire dall’id)
            String barberDisplay = bean.getBarbiereId();
            try {
                GenericDao<Barbiere> barbiereDao = DaoFactory.getInstance().getBarbiereDao();
                Barbiere b = barbiereDao.read(bean.getBarbiereId());
                if (b != null) {
                    String full = (safe(b.getFirstName()) + " " + safe(b.getLastName())).trim();
                    if (!full.isBlank()) barberDisplay = full;
                }
            } catch (Exception ignore) {
                // Se fallisce, mostriamo l’id del barbiere
            }

            // 3) Subject + body
            String subject = "Conferma Prenotazione" +
                    (bean.getDay() == null ? "" : (" " + bean.getDay().format(DATE_FMT)));

            String dateStr = bean.getDay() == null ? "-" : bean.getDay().format(DATE_FMT);
            String timeStr = (bean.getStartTime() == null || bean.getEndTime() == null)
                    ? "-" : (bean.getStartTime().format(TIME_FMT) + " - " + bean.getEndTime().format(TIME_FMT));

            // Se in sessione hai anche nome/cognome utente, recuperali qui; altrimenti usa l’email come saluto
            String customerDisplayName = defaultIfBlank(SessionManager.getInstance().getCurrentSession().getDisplayName(),
                    toEmail);

            String body = """
                Gentile %s,

                la tua prenotazione è stata confermata con successo.

                Dettagli:
                • Servizio: %s
                • Professionista: %s
                • Data: %s
                • Orario: %s
                • Importo: %s

                Per qualsiasi domanda, contattaci pure.
                A presto!
                """.formatted(
                    customerDisplayName,
                    servizioName,
                    barberDisplay,
                    dateStr,
                    timeStr,
                    prezzoStr
            );

            // 4) Invio (simulato)
            EmailService.sendEmail(toEmail, subject, body);
        }

        private static String safe(String s) { return s == null ? "" : s; }
        private static String defaultIfBlank(String s, String def) {
            return (s == null || s.isBlank()) ? def : s;
        }
    public static String processPayment(String intestatario,String cardNumber,String expire, String cvv, BigDecimal amount) {

        return PaymentService.processPayment(intestatario,cardNumber, expire, cvv, amount);

    }
    }


