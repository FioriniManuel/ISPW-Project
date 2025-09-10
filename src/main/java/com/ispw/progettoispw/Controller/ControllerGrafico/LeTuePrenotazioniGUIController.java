package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.event.ActionEvent;


import java.io.IOException;


public class LeTuePrenotazioniGUIController {

    @FXML
    private Button esciLeTuePrenotazioni;

    public void initialize() {

    }


    public void esciLeTuePrenotazionionAction (ActionEvent event) {
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}