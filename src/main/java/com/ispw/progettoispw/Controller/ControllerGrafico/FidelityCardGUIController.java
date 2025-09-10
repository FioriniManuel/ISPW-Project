package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.FidelityController;
import com.ispw.progettoispw.Session.Session;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.FidelityBean;
import com.ispw.progettoispw.bean.PrizeBean;          // <- view model, non entity
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class FidelityCardGUIController {

    // --- dal tuo FXML ---
    @FXML private Label infoLabel;

    @FXML private Label puntiRimastiLabel;

    // colonne premi
    @FXML private Label puntiPrimoPremioLabel;
    @FXML private Label puntiSecondoPremioLabel;
    @FXML private Label puntiTerzoPremioLabel;

    @FXML private Label primoPremioLabel;
    @FXML private Label secondoPremioLabel;
    @FXML private Label terzoPremioLabel;

    // RadioButton con fx:id nel tuo FXML
    @FXML private RadioButton prize1Radio;
    @FXML private RadioButton prize2Radio;
    @FXML private RadioButton prize3Radio;

    // --- service & stato ---
    private final FidelityController fidelityService = new FidelityController();
    private final ToggleGroup prizeGroup = new ToggleGroup();
    private final FidelityBean bean = new FidelityBean();

    private List<PrizeBean> prizes; // P1, P2, P3
    private String clientId;

    @FXML
    private void initialize() {
        hideInfo();

        // utente loggato
        Session s = SessionManager.getInstance().getCurrentSession();
        if (s == null) {
            showInfo("Effettua l'accesso per usare la fidelity.");
            disableActions(true);
            return;
        }
        clientId = s.getId();

        // radio in un unico gruppo
        prize1Radio.setToggleGroup(prizeGroup);
        prize2Radio.setToggleGroup(prizeGroup);
        prize3Radio.setToggleGroup(prizeGroup);

        // carica premi + punti
        reloadData();

        // selection listener
        prizeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                bean.setSelectedPrizeId(null);
                return;
            }
            if (newT == prize1Radio) bean.setSelectedPrizeId("P1");
            else if (newT == prize2Radio) bean.setSelectedPrizeId("P2");
            else if (newT == prize3Radio) bean.setSelectedPrizeId("P3");
        });
    }

    private void reloadData() {
        // punti
        int points = fidelityService.getCustomerPoints(clientId);
        bean.setTotalPoints(points);
        puntiRimastiLabel.setText(String.valueOf(points));

        // premi (VM, non entity)
        prizes = fidelityService.listPrizesVM();
        bean.setPrizes(prizes);

        // mappo P1,P2,P3 sui label
        PrizeBean p1 = find("P1"), p2 = find("P2"), p3 = find("P3");
        if (p1 != null) {
            puntiPrimoPremioLabel.setText(String.valueOf(p1.getRequiredPoints()));
            primoPremioLabel.setText(descrizionePremio(p1));
        }
        if (p2 != null) {
            puntiSecondoPremioLabel.setText(String.valueOf(p2.getRequiredPoints()));
            secondoPremioLabel.setText(descrizionePremio(p2));
        }
        if (p3 != null) {
            puntiTerzoPremioLabel.setText(String.valueOf(p3.getRequiredPoints()));
            terzoPremioLabel.setText(descrizionePremio(p3));
        }

        // abilita/disabilita radiobutton se non ho abbastanza punti
        if (p1 != null) prize1Radio.setDisable(points < p1.getRequiredPoints());
        if (p2 != null) prize2Radio.setDisable(points < p2.getRequiredPoints());
        if (p3 != null) prize3Radio.setDisable(points < p3.getRequiredPoints());
    }

    private PrizeBean find(String id) {
        if (prizes == null) return null;
        return prizes.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    private String descrizionePremio(PrizeBean p) {
        BigDecimal v = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        return p.getName() + " — Coupon € " + v.toPlainString();
    }

    private void disableActions(boolean b) {
        prize1Radio.setDisable(b);
        prize2Radio.setDisable(b);
        prize3Radio.setDisable(b);
    }

    private void showInfo(String msg) {
        if (infoLabel != null) {
            infoLabel.setText(msg);
            infoLabel.setVisible(true);
        }
    }

    private void hideInfo() {
        if (infoLabel != null) {
            infoLabel.setVisible(false);
            infoLabel.setText("");
        }
    }

    /* =================== Handlers =================== */

    @FXML
    private void ritiraPremioOnAction() {
        hideInfo();
        String prizeId = bean.getSelectedPrizeId();
        if (prizeId == null) {
            showInfo("Seleziona un premio.");
            return;
        }
        if (!fidelityService.canRedeem(clientId, prizeId)) {
            showInfo("Punti insufficienti per il premio selezionato.");
            return;
        }
        try {
            String code = fidelityService.redeem(clientId, prizeId);
            showInfo("Premio riscattato! Coupon generato: " + code);
            // aggiorna punti e abilitazioni
            reloadData();
            prizeGroup.selectToggle(null);
            bean.setSelectedPrizeId(null);
        } catch (Exception ex) {
            showInfo("Errore durante il riscatto: " + ex.getMessage());
        }
    }

    @FXML
    private void homeButtonOnAction() {
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
