package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;


import java.io.IOException;

public class LoginGUIController {
    @FXML
    private Hyperlink registrationHyperlink;
    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField enterPasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private ImageView brandingImageView;
    @FXML
    private CheckBox barbiereCheckBox;


    public void initialize() {
        File brandingFile = new File("Imagini/Logo.png");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        brandingImageView.setImage(brandingImage);


    }

    public void loginButtonAction(ActionEvent event) {
        loginMessageLabel.setText("You try to login");
        if (usernameTextField.getText().isBlank() == false && enterPasswordField.getText().isBlank() == false) {
            try { if(barbiereCheckBox.isSelected()){
                WindowManager.getInstance().switchScene("HomeBarbiereView.fxml", "Home");

            } else{
                WindowManager.getInstance().switchScene("HomeView.fxml","Home");

            }} catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            loginMessageLabel.setText("Please enter username and password");

        }
    }


    public void registrationHyperlinkOnAction(ActionEvent event) {
        try {
            WindowManager.getInstance().switchScene("RegistrationView.fxml","Registrazione");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}