package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.Region;


import java.io.IOException;
import java.util.Optional;

public class HomeGUIController {
        @FXML
        private Label welcomeLabel;
        @FXML
        private Button profiloButton;
        @FXML
        private Button shopButton;
        @FXML
        private Button prenotaAppuntamentoButton;

        LoginController loginText= new LoginController();

        public void initialize() {
            String nomeCompleto = loginText.getName(); // es: "Mario Rossi"
            welcomeLabel.setText(nomeCompleto);
        }







    public void prenotaAppuntamentoButtonOnAction (ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("PrenotazioneView.fxml","Listino");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }

    public void fidelityCardOnAction (ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("FidelityCardView.fxml","Fidelity Card");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }

    public void leTuePrenotazioniButtonOnAction (ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("LeTuePrenotazioniView.fxml","Le Tue Prenotazioni");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void exitButtonOnAction(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Conferma uscita");
        confirm.setHeaderText("Vuoi davvero uscire?");
        confirm.setContentText("Premi Conferma per disconnetterti oppure Annulla per restare.");
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulla  = new ButtonType("Annulla",  ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(conferma, annulla);

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == conferma) {
            // logout
            loginText.logOut();
            // torna alla login
            try {
                WindowManager.getInstance().switchScene("LoginView.fxml", "Login");
            } catch (IOException e) {
                showError("Navigazione", "Impossibile tornare alla schermata di login.");
            }
        }
        // se annulla: non fare nulla
    }

    // ---- Helpers ----
    private static void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.showAndWait();
    }


}
