package com.ispw.progettoispw.ControllerGrafico;
import javafx.fxml.FXML;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.IOException;

public class RegistrationGUIController {
    @FXML
    private Hyperlink loginHyperLink;
    @FXML
    private Button registerButton;

    public void initialize() {

    }

    public void registerButtonOnAction (ActionEvent event){
        try {
            WindowManager.getInstance().switchScene("HomeView.fxml","Home");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void loginHyperlinkOnAction (ActionEvent event){
        try{  WindowManager.getInstance().switchScene("LoginView.fxml", "ZAC ZAC");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }


}
    }





