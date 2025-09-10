package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

public class IniziamoGUIController {


    public void initialize() {

    }




    public void loginButtonOnAction (ActionEvent event){
        try{  WindowManager.getInstance().switchScene("LoginView.fxml", "ZAC ZAC");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void prenotazioneButtonOnAction (ActionEvent event){
        try{  WindowManager.getInstance().switchScene("ItuoiDatiView.fxml", "Inserisci i tuoi Dati per la Prenotazione");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }


    }
}

