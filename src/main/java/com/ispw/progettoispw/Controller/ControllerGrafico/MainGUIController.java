package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

public class MainGUIController {
    public void initialize() {

    }

    public void continueButtonOnAction (ActionEvent event){
        try {
            WindowManager.getInstance().switchScene("IniziamoView.fxml","Iniziamo");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
