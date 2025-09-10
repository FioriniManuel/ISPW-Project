package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

public class PagamentoGUIController {


    public void initialize() {

    }



    public void homeButtonOnAction (ActionEvent event) {
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
