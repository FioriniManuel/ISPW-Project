package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class HomeBarbiereGUIController {

    @FXML private Label nomeBarbiere;
    @FXML private Button btnEsci;

    private final LoginController loginController = new LoginController();
    private String barberId;
    private String barberName;

    @FXML
    private void initialize() {
        // Recupera dati barbiere dalla sessione
        this.barberId   = loginController.getId();
        this.barberName = LoginController.getName();

        if (barberName != null && !barberName.isBlank()) {
            nomeBarbiere.setText(barberName);
        } else {
            nomeBarbiere.setText("Barbiere");
        }

        // Handler esci
        btnEsci.setOnAction(e -> doLogout());
    }

    @FXML
    private void listaPrenotazioniOnAction() {
        // Apri la schermata con la lista prenotazioni del barbiere (giorno selezionabile)
        try {
            WindowManager.getInstance().switchScene(
                    "ListaPrenotazioniView.fxml",                // <-- metti qui il tuo FXML della lista prenotazioni
                    "Prenotazioni");// passa contesto barbiere

        } catch (IOException ex) {
            showError("Impossibile aprire la lista prenotazioni: " + ex.getMessage());
        }
    }

    @FXML
    private void notImplementedOnAction() {
        // Se vuoi davvero navigare alla gestione Fidelity:
        try {
            WindowManager.getInstance().switchScene(
                    "GestisciFidelityCardView.fxml",           // <-- FXML della gestione fidelity che hai creato
                    "Gestione Fidelity"
            );
        } catch (IOException ex) {
            showError("Impossibile aprire la Gestione Fidelity: " + ex.getMessage());
        }

        // Se invece vuoi solo un messaggio, commenta il blocco sopra e usa:
        // showInfo("Funzionalità non ancora implementata.");
    }

    private void doLogout() {
        try {
            loginController.logOut();
            WindowManager.getInstance().switchScene("LoginView.fxml", "Zac Zac"); // oppure "LoginView.fxml"
        } catch (IOException ex) {
            showError("Errore durante il logout: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
