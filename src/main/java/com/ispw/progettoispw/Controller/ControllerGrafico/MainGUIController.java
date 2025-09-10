package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.MainController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;

public class MainGUIController {
    @FXML
    private ToggleButton togglePersistenza;
    @FXML
    private RadioButton primaInterfaccia;
    @FXML
    private RadioButton secondaInterfaccia;

    MainController reg = new MainController();

    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        primaInterfaccia.setToggleGroup(group);
        secondaInterfaccia.setToggleGroup(group);

        // opzionale: imposta selezionato di default
        primaInterfaccia.setSelected(true);
    }

    public void continueButtonOnAction(ActionEvent event) {

        if (togglePersistenza.isSelected()) {
            reg.persistenza();
        } else {
            reg.memory();
        }

        if (primaInterfaccia.isSelected()) {
            try {
                WindowManager.getInstance().switchScene("IniziamoView.fxml", "Iniziamo");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        } else if(secondaInterfaccia.isSelected()) {
            try {
                WindowManager.getInstance().switchScene("LoginViewAlternative.fxml", "Login");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}