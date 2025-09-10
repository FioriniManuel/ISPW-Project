package com.ispw.progettoispw.ApplicationFacade;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    private PaymentService(){
        //privato, nasconde costruttore pubblico
    }

    /**
     * Simula un pagamento e restituisce un esito positivo o negativo.
     * @param cardNumber Numero della carta di credito.
     * @param amount Importo da pagare.

     * @return `true` se il pagamento ha avuto successo, `false` se fallisce.
     */
    public static String processPayment(String intestatario, String cardNumber, String data, String cvv, BigDecimal amount) {
        logger.info("Simulazione pagamento...");
        logger.log(Level.INFO,"Intestatario: {0}",intestatario);
        logger.log(Level.INFO, "Numero carta: **** **** **** {0}", cardNumber.substring(cardNumber.length() - 4));
        logger.log(Level.INFO, "Controllo data di scadenza... {0}",data);
        logger.log(Level.INFO,"Controllo CVV: {0}",cvv);
        logger.log(Level.INFO, "Importo: {0} {1}", new Object[]{amount, "€"});

        // Simuliamo una probabilità di successo del 90%
        if( Math.random() < 0.9){
            return "success";
        }
        else{return "unsuccess";}
    }
}
