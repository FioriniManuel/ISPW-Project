package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.RegistrationBean;
import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.List;

public class RegistrazioneClienteGUIAlternativeController {

    // --- fx:id dal tuo FXML ---
    @FXML private TextField nameLabel;          // Nome
    @FXML private TextField cognomeLabel;       // Cognome
    @FXML private TextField emailLabel;         // Email
    @FXML private TextField phoneLabel;         // Telefono
    @FXML private PasswordField passwordLabel;  // Password
    @FXML private PasswordField confirmPassword;// Conferma Password

    // Bottoni/Link hanno già onAction nel FXML
    // onRegister -> #onRegister
    // onLogin    -> #onLogin
    // onLink     -> #onLink  (link "Sei un Parrucchiere?...Registrati")

    private final RegistrazioneController appController = new RegistrazioneController();

    @FXML
    public void initialize() {
        // Qualità di vita: Invio = "Registrati"
        // (se vuoi, puoi trovare il bottone via lookup; non strettamente necessario)
        // ((Button) root.lookup("#registerButton")).setDefaultButton(true);
    }

    @FXML
    private void onRegister() {
        // Costruisci la bean
        RegistrationBean bean = new RegistrationBean();
        bean.setUserType(Role.CLIENTE); // questo controller è per i clienti

        bean.setFirstName(safeTrim(nameLabel.getText()));
        bean.setLastName(safeTrim(cognomeLabel.getText()));
        bean.setEmail(safeTrim(emailLabel.getText()));
        bean.setPhoneNumber(safeTrim(phoneLabel.getText()));
        bean.setPassword(nullSafe(passwordLabel.getText()));
        bean.setRepeatPassword(nullSafe(confirmPassword.getText()));

        // Validazione lato bean (regex email, telefono IT, pwd 8..16, match pwd, ecc.)
        List<String> errors = bean.validate();
        if (!errors.isEmpty()) {
            showAlert(AlertType.WARNING, "Errori nel form", String.join("\n", errors));
            return;
        }

        // Chiamata al controller applicativo
        String esito = appController.register(bean);

        switch (esito) {
            case "success" -> {
                showAlert(AlertType.INFORMATION, "Registrazione completata",
                        "Account cliente creato con successo. Ora effettua il login.");
                goTo("LoginViewAlternative.fxml", "Login");
            }
            case "error:validation" -> showAlert(AlertType.WARNING, "Dati non validi",
                    "Controlla i campi inseriti.");
            case "error:email_exists" -> showAlert(AlertType.ERROR, "Email già registrata",
                    "Questa email risulta già presente.");
            case "error:phone_exists" -> showAlert(AlertType.ERROR, "Telefono già registrato",
                    "Questo numero risulta già presente.");
            case "error:conflict" -> showAlert(AlertType.ERROR, "Conflitto dati",
                    "Alcuni dati risultano duplicati.");
            case "error:unexpected" -> showAlert(AlertType.ERROR, "Errore inatteso",
                    "Si è verificato un errore interno. Riprova più tardi.");
            case "error:unknown_user_type" -> showAlert(AlertType.ERROR, "Tipo utente non valido",
                    "Tipo utente non riconosciuto.");
            default -> showAlert(AlertType.ERROR, "Errore",
                    "Risposta non prevista dal server: " + esito);
        }
    }

    @FXML
    private void onLogin() {
        goTo("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onLink() {
        // Link: "Sei un Parrucchiere?...Registrati"
        goTo("RegistrationBarbiereViewAlternative.fxml", "Registrazione Barbiere");
    }

    // -------- util --------
    private void goTo(String fxmlRelativePath, String title) {
        try {
            // La tua WindowManager risolve "/com/ispw/progettoispw/" + fxmlRelativePath
            WindowManager.getInstance().switchScene(fxmlRelativePath, title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Caricamento fallito",
                    "Impossibile aprire la schermata: " + fxmlRelativePath);
        }
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
    private static String nullSafe(String s) { return s == null ? "" : s; }

    private void showAlert(AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle("Messaggio");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
