package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;

public class HomeGUIAlternativeController {

    @FXML private Label nameLabel;
    @FXML private RadioButton areaPersonaleRadio;
    @FXML private RadioButton prenotaRadio;
    @FXML private Button backButton;
    @FXML private Button continueButton;

    private ToggleGroup choiceGroup;

    @FXML
    public void initialize() {
        // Mostra nome utente (se disponibile) dal controller applicativo
        String displayName = LoginController.getName(); // usa la tua API applicativa
        if (displayName == null || displayName.isBlank()) {
            nameLabel.setText("Ciao!");
        } else {
            nameLabel.setText("Ciao, " + displayName + "!");
        }

        // ToggleGroup per i due RadioButton
        choiceGroup = new ToggleGroup();
        areaPersonaleRadio.setToggleGroup(choiceGroup);
        prenotaRadio.setToggleGroup(choiceGroup);

        // UX: Enter = Continua, Esc = Indietro
        continueButton.setDefaultButton(true);
        backButton.setCancelButton(true);
    }

    @FXML
    private void onBack() {
        // Torna al login (cambia path/titolo se vuoi un'altra schermata)
        LoginController.logOut();
        goTo("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onContinue() {
        if (choiceGroup.getSelectedToggle() == null) {
            warn("Selezione richiesta", "Scegli un'opzione prima di continuare.");
            return;
        }

        if (areaPersonaleRadio.isSelected()) {
            // Vai all'area personale del ruolo corrente (cliente/barbiere)
            // Se vuoi distinguere per ruolo, puoi leggere il ruolo dal tuo Session/Controller applicativo
            goTo("AreaPersonaleViewAlternative.fxml", "Area Personale");
        } else if (prenotaRadio.isSelected()) {
            // Vai al flusso prenotazione
            goTo("PrenotazioneViewAlternative.fxml", "Prenota Appuntamento");
        }
    }

    // --------- utility ---------
    private void goTo(String fxmlRelativePath, String title) {
        try {
            WindowManager.getInstance().switchScene(fxmlRelativePath, title);
        } catch (IOException e) {
            e.printStackTrace();
            error("Caricamento fallito", "Impossibile aprire la schermata: " + fxmlRelativePath);
        }
    }

    private void warn(String header, String content) {
        Alert a = new Alert(AlertType.WARNING);
        a.setTitle("Attenzione");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void error(String header, String content) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
