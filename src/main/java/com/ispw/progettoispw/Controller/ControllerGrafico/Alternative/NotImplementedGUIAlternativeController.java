package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import java.io.IOException;

public class NotImplementedGUIAlternativeController {
    public void initialize(){

    }

    @FXML
    public void indietroButtonOnAction() throws IOException {
        WindowManager.getInstance().switchScene("HomeBarbiereViewAlternative.fxml", "Home");
    }
}
