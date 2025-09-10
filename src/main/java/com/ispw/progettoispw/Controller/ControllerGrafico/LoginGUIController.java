package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.LoginBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

import java.io.File;
import java.io.IOException;

public class LoginGUIController {

    @FXML private Hyperlink registrationHyperlink;
    @FXML private Label loginMessageLabel;
    @FXML private TextField emailTextField;      // ⚠️ allineato all’FXML (era emailTextField)
    @FXML private PasswordField enterPasswordField;
    @FXML private Button loginButton;
    @FXML private ImageView brandingImageView;
    @FXML private CheckBox barbiereCheckBox;

    private final LoginController loginService = new LoginController();

    @FXML
    public void initialize() {
        // logo
        File brandingFile = new File("Imagini/Logo.png");
        if (brandingFile.exists()) {
            Image brandingImage = new Image(brandingFile.toURI().toString());
            brandingImageView.setImage(brandingImage);
        }
        // pulizia messaggi
        if (loginMessageLabel != null) loginMessageLabel.setText("");
    }

    @FXML
    public void loginButtonAction(ActionEvent event) throws IOException {
        // reset messaggi
        if (loginMessageLabel != null) loginMessageLabel.setText("");

        // 1) Bean da UI
        LoginBean bean = new LoginBean();
        bean.setEmail(trimOrEmpty(emailTextField));
        bean.setPassword(trimOrEmpty(enterPasswordField));


        if (barbiereCheckBox != null && barbiereCheckBox.isSelected()) {
            bean.setUserType(Role.BARBIERE);
        } else {
            bean.setUserType(Role.CLIENTE);
        }

        // 2) Login
        String outcome = loginService.login(bean);

        // 3) Esito
        switch (outcome) {
            case "success:cliente" -> {
                showInfo("Accesso effettuato", "Benvenuto!");
                WindowManager.getInstance().switchScene("HomeView.fxml", "Home");
            }
            case "success:barbiere" -> {
                showInfo("Accesso effettuato", "Benvenuto!");
                WindowManager.getInstance().switchScene("HomeBarbiereView.fxml", "Home");
            }
            case "error:validation" -> setError("Inserisci email e password.");
            case "error:not_found" -> setError("Utente non trovato. Controlla l'email o registrati.");
            case "error:wrong_credentials" -> setError("Credenziali errate. Riprova.");
            default -> setError("Si è verificato un errore. (" + outcome + ")");
        }
    }

    @FXML
    public void registrationHyperlinkOnAction(ActionEvent event) {
        try {
            WindowManager.getInstance().switchScene("RegistrationView.fxml", "Registrazione");
        } catch (IOException e) {
            showError("Navigazione", "Impossibile aprire la schermata di registrazione.");
        }
    }

    // ---- helpers UI ----
    private void setError(String msg) {
        if (loginMessageLabel != null) {
            loginMessageLabel.setText(msg);
        } else {
            showError("Errore", msg);
        }
    }

    private static String trimOrEmpty(TextField f) {
        String s = (f != null) ? f.getText() : null;
        return (s == null) ? "" : s.trim();
    }

    private static String trimOrEmpty(PasswordField f) {
        String s = (f != null) ? f.getText() : null;
        return (s == null) ? "" : s.trim();
    }

    private static void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.showAndWait();
    }

    private static void showInfo(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.showAndWait();
    }
}
