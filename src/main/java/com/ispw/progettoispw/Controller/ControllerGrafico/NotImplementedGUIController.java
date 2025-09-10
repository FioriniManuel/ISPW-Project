package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import java.io.IOException;

public class NotImplementedGUIController {
    public void initialize(){

    }

    @FXML
    public void indietroButtonOnAction() throws IOException {
        WindowManager.getInstance().switchScene("HomeBarbiereView.fxml", "Home");
    }


}
