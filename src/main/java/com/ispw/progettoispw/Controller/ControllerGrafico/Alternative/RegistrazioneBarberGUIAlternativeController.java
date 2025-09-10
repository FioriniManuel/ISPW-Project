package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.RegistrationBean;
import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;

public class RegistrazioneBarberGUIAlternativeController {

    // --- campi form ---
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;

    // --- radio specializzazione (Donna/Uomo) ---
    @FXML private RadioButton donnaRadio;
    @FXML private RadioButton uomoRadio;
    private ToggleGroup genderGroup;

    // --- bottoni/link ---
    @FXML private Button registerButton;
    @FXML private Button goLoginButton;
    @FXML private Hyperlink goClientRegLink;

    private final RegistrazioneController appController = new RegistrazioneController();

    @FXML
    public void initialize() {
        // ToggleGroup per i radio di specializzazione
        genderGroup = new ToggleGroup();
        donnaRadio.setToggleGroup(genderGroup);
        uomoRadio.setToggleGroup(genderGroup);
        // default: scegli tu quale mettere di base
        donnaRadio.setSelected(true);

        // Qualità di vita: Invio = Registrati

    }

    @FXML
    private void onRegister() {
        // Costruisci la bean
        RegistrationBean bean = new RegistrationBean();
        bean.setUserType(Role.BARBIERE);
        bean.setFirstName(safeTrim(firstNameField.getText()));
        bean.setLastName(safeTrim(lastNameField.getText()));
        bean.setEmail(safeTrim(emailField.getText()));
        bean.setPhoneNumber(safeTrim(phoneField.getText()));
        bean.setPassword(passwordField.getText());
        bean.setRepeatPassword(repeatPasswordField.getText());

        // Mappa specializzazione da radio
        if (genderGroup.getSelectedToggle() == null) {
            warn("Specializzazione mancante", "Seleziona Donna o Uomo.");
            return;
        }
        // ATTENZIONE: usa i tuoi literal dell’enum GenderCategory.
        // Se il tuo enum è DONNA/UOMO, usa quelli.
        bean.setSpecializzazione(donnaRadio.isSelected() ? GenderCategory.DONNA : GenderCategory.UOMO);

        // Valida lato bean (mostro eventuali errori raccolti)
        var errors = bean.validate();
        if (!errors.isEmpty()) {
            warn("Errori nel form", String.join("\n", errors));
            return;
        }

        // Chiama il controller applicativo
        String esito = appController.register(bean);

        switch (esito) {
            case "success" -> {
                info("Registrazione completata", "Barbiere registrato con successo.");
                // vai al login (o alla home barber, decidi tu)
                goTo("LoginViewAlternative.fxml", "Login");
            }
            case "error:validation" -> warn("Dati non validi", "Controlla i campi inseriti.");
            case "error:email_exists" -> error("Email già registrata", "Questa email risulta già presente.");
            case "error:phone_exists" -> error("Telefono già registrato", "Questo numero risulta già presente.");
            case "error:conflict" -> error("Conflitto dati", "Qualche dato risulta duplicato.");
            case "error:unexpected" -> error("Errore inatteso", "Si è verificato un errore interno.");
            case "error:unknown_user_type" -> error("Tipo utente non valido", "Tipo utente non riconosciuto.");
            default -> error("Errore", "Risposta non prevista: " + esito);
        }
    }

    @FXML
    private void onGoLogin() {
        goTo("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onGoClientRegistration() {
        // Link “Sei un Cliente?...Registrati”
        goTo("RegistrationClienteViewAlternative.fxml", "Registrazione Cliente");
    }

    // ---------- util ----------

    private void goTo(String fxml, String title) {
        try {
            WindowManager.getInstance().switchScene(fxml, title);
        } catch (IOException e) {
            e.printStackTrace();
            error("Caricamento fallito", "Impossibile aprire la schermata: " + fxml);
        }
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private void info(String header, String content) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle("Informazione");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void warn(String header, String content) {
        Alert a = new Alert(AlertType.WARNING);
        a.setTitle("Attenzione");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void error(String header, String content) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
