package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.FidelityController;
import com.ispw.progettoispw.Session.Session;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.PrizeBean;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class FidelityCardGUIAlternativeController {

    // --- UI: punti disponibili ---
    @FXML private Label puntiRimastiLabel;

    // --- UI: punti necessari (3 righe) ---
    @FXML private Label puntiNec1Label;
    @FXML private Label puntiNec2Label;
    @FXML private Label puntiNec3Label;

    // --- UI: descrizione premio (3 righe) ---
    @FXML private Label premio1Label;
    @FXML private Label premio2Label;
    @FXML private Label premio3Label;

    // --- UI: pulsanti redeem ---
    @FXML private Button redeemBtn1;
    @FXML private Button redeemBtn2;
    @FXML private Button redeemBtn3;

    // --- UI: nav ---
    @FXML private Button backButton;
    @FXML private Button homeButton;

    // --- servizi/stato ---
    private final FidelityController fidelityService = new FidelityController();
    private List<PrizeBean> prizes; // atteso P1,P2,P3
    private String clientId;

    @FXML
    private void initialize() {
        // ricava utente
        Session s = SessionManager.getInstance().getCurrentSession();
        if (s == null) {
            setAllDisabled(true);
            if (puntiRimastiLabel != null) puntiRimastiLabel.setText("0");
            showInfo("Effettua l'accesso per usare la Fidelity Card.");
            return;
        }
        clientId = s.getId();

        // carica dati
        reloadData();
    }

    private void reloadData() {
        // Punti disponibili
        int points = fidelityService.getCustomerPoints(clientId);
        puntiRimastiLabel.setText(String.valueOf(points));

        // Premi (VM, non entity)
        prizes = fidelityService.listPrizesVM();

        // mappa e abilita/disabilita i bottoni in base ai punti
        PrizeBean p1 = find("P1"), p2 = find("P2"), p3 = find("P3");

        if (p1 != null) {
            puntiNec1Label.setText(String.valueOf(p1.getRequiredPoints()));
            premio1Label.setText(descrizionePremio(p1));
            redeemBtn1.setDisable(points < p1.getRequiredPoints());
        } else {
            puntiNec1Label.setText("-");
            premio1Label.setText("-");
            redeemBtn1.setDisable(true);
        }

        if (p2 != null) {
            puntiNec2Label.setText(String.valueOf(p2.getRequiredPoints()));
            premio2Label.setText(descrizionePremio(p2));
            redeemBtn2.setDisable(points < p2.getRequiredPoints());
        } else {
            puntiNec2Label.setText("-");
            premio2Label.setText("-");
            redeemBtn2.setDisable(true);
        }

        if (p3 != null) {
            puntiNec3Label.setText(String.valueOf(p3.getRequiredPoints()));
            premio3Label.setText(descrizionePremio(p3));
            redeemBtn3.setDisable(points < p3.getRequiredPoints());
        } else {
            puntiNec3Label.setText("-");
            premio3Label.setText("-");
            redeemBtn3.setDisable(true);
        }
    }

    private PrizeBean find(String id) {
        if (prizes == null) return null;
        return prizes.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    private String descrizionePremio(PrizeBean p) {
        BigDecimal v = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        return p.getName() + " — Coupon € " + v.toPlainString();
    }

    private void setAllDisabled(boolean disabled) {
        if (redeemBtn1 != null) redeemBtn1.setDisable(disabled);
        if (redeemBtn2 != null) redeemBtn2.setDisable(disabled);
        if (redeemBtn3 != null) redeemBtn3.setDisable(disabled);
        if (backButton != null)  backButton.setDisable(disabled);
        if (homeButton != null)  homeButton.setDisable(disabled);
    }

    /* =================== Handlers =================== */

    @FXML private void onRedeemP1() { redeem("P1"); }
    @FXML private void onRedeemP2() { redeem("P2"); }
    @FXML private void onRedeemP3() { redeem("P3"); }

    private void redeem(String prizeId) {
        if (clientId == null) {
            showInfo("Utente non loggato.");
            return;
        }
        if (!fidelityService.canRedeem(clientId, prizeId)) {
            showInfo("Punti insufficienti per il premio selezionato.");
            return;
        }
        try {
            String code = fidelityService.redeem(clientId, prizeId);
            showInfo("Premio riscattato! Coupon generato: " + code);
            reloadData(); // aggiorna punti e pulsanti
        } catch (Exception ex) {
            showInfo("Errore durante il riscatto: " + ex.getMessage());
        }
    }

    @FXML
    private void onBack() {
        try {
            WindowManager.getInstance().switchScene("AreaPersonaleViewAlternative.fxml", "Area Personale");
        } catch (IOException e) {
            showError("Caricamento fallito", "Impossibile aprire la schermata Area Personale.");
        }
    }

    @FXML
    private void onHome() {
        try {
            WindowManager.getInstance().switchScene("HomeViewAlternative.fxml", "Home");
        } catch (IOException e) {
            showError("Caricamento fallito", "Impossibile aprire la schermata Home.");
        }
    }

    /* =================== Dialog util =================== */

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
