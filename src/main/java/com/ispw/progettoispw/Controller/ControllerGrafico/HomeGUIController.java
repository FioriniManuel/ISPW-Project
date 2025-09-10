package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.event.ActionEvent;


import java.io.IOException;

public class HomeGUIController {

        @FXML
        private Button profiloButton;
        @FXML
        private Button shopButton;
        @FXML
        private Button prenotaAppuntamentoButton;

        public void initialize() {

        }



        public void profiloButtonOnAction (ActionEvent event) {
            try {
                WindowManager.getInstance().switchScene("GestioneProfiloView.fxml", "Gestione Profilo");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        public void shopButtonOnAction (ActionEvent event){
            try{
                WindowManager.getInstance().switchScene("ShopZacZacView.fxml","Shop");
            } catch(IOException e){
                throw new RuntimeException(e);
            }
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

}
