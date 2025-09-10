package com.ispw.progettoispw.ApplicationFacade;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    private EmailService() {
        //nasconde il costruttore pubblico
    }

    /**
     * Simula l'invio di un'email.
     * @param toEmail Indirizzo email del destinatario.
     * @param subject Oggetto dell'email.
     * @param body Contenuto dell'email.
     */
    public static void sendEmail(String toEmail, String subject, String body) {
        logger.info("Simulazione invio email...");
        logger.log(Level.INFO, "Destinatario: {0}", toEmail);
        logger.log(Level.INFO, "Oggetto: {0}", subject);
        logger.log(Level.INFO, "Contenuto: {0}", body);
        logger.info("Email inviata con successo (simulato).");
    }
}