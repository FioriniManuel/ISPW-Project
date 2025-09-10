package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.LoginBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginGUIAlternativeController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton clienteRadio;
    @FXML private RadioButton barbiereRadio;
    @FXML private Button loginButton;
    @FXML private Button registrazioneButton;

    private ToggleGroup roleGroup;
    private final LoginController appController = new LoginController();

    @FXML
    public void initialize() {
        // ToggleGroup per i radio
        roleGroup = new ToggleGroup();
        clienteRadio.setToggleGroup(roleGroup);
        barbiereRadio.setToggleGroup(roleGroup);
        // default
        clienteRadio.setSelected(true);

        loginButton.setDefaultButton(true);
    }

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        String email = safeTrim(emailField.getText());
        String pwd   = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty()) { warn("Email mancante", "Inserisci l'email."); return; }
        if (pwd.isEmpty())   { warn("Password mancante", "Inserisci la password."); return; }

        LoginBean bean = new LoginBean();
        bean.setEmail(email);
        bean.setPassword(pwd);
        if(clienteRadio.isSelected()){
        bean.setUserType(Role.CLIENTE);}
        else{bean.setUserType(Role.BARBIERE);}
        // NB: qui il ruolo NON serve, lo deciderà il controller applicativo
        // bean.setUserType(null);

        String esito = appController.login(bean);

        switch (esito) {
            case "success:cliente"  -> WindowManager.getInstance().switchScene("HomeViewAlternative.fxml","Home");
            case "success:barbiere" -> WindowManager.getInstance().switchScene("HomeBarbiereViewAlternative.fxml","Home Barbiere");
            case "error:validation" -> warn("Dati non validi", "Controlla email e password.");
            case "error:not_found"  -> error("Utente non trovato", "Nessun account corrisponde all'email inserita.");
            case "error:wrong_credentials" -> error("Credenziali errate", "Email o password non corretti.");
            default -> error("Errore imprevisto", "Risposta: " + esito);
        }
    }

    @FXML
    private void onRegistrazione(ActionEvent event) throws IOException {
        String nextFxml;
        String title;

        if (clienteRadio.isSelected()) {
            WindowManager.getInstance().switchScene("RegistrationClienteViewAlternative.fxml","Registrazione");
        } else if (barbiereRadio.isSelected()) {
            WindowManager.getInstance().switchScene("RegistrationBarbiereViewAlternative.fxml","Registrazione");

        } else {
            warn("Ruolo non selezionato", "Seleziona Cliente o Barbiere prima di registrarti.");
            return;
        }

    }

    // -------- utilità --------


    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private void warn(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attenzione");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void error(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
