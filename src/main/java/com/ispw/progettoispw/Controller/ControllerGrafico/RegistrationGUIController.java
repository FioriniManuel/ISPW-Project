package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.bean.RegistrationBean;
import com.ispw.progettoispw.Enum.*;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class RegistrationGUIController {

    @FXML private TextField nomeTextField;
    @FXML private TextField cognomeTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField telefonoTextField;
    @FXML private PasswordField setPasswordField;
    @FXML private PasswordField confirmPassword;
    @FXML private Hyperlink loginHyperLink;
    @FXML private Button registerButton;
    @FXML private CheckBox barberCheckBox;
    @FXML private Label visibleLabel;
    @FXML private ToggleButton donnaVisibleToggleButton;
    @FXML private ToggleButton uomoVisibleToggleButton;

    // Se in Scene Builder hai impostato il ToggleGroup con fx:id="genderGroup", lascia questa riga:

    // Se NON l’hai impostato in FXML, decommenta le 2 righe sotto e commenta quella sopra:
    private final ToggleGroup genderGroup = new ToggleGroup();

    private final RegistrazioneController registrazioneController = new RegistrazioneController();

    public void initialize() {
        // Se NON usi ToggleGroup da FXML, collega da codice:

         uomoVisibleToggleButton.setToggleGroup(genderGroup);
         donnaVisibleToggleButton.setToggleGroup(genderGroup);

        // Avvio: nascondi controlli "genere"
        setGenderControlsVisible(false);

        // Mostra/nascondi i controlli quando la checkbox cambia
        barberCheckBox.selectedProperty().addListener((obs, oldVal, isBarber) -> {
            setGenderControlsVisible(isBarber);
            if (!isBarber && genderGroup != null) {
                genderGroup.selectToggle(null); // reset scelta
            }
        });
    }

    private void setGenderControlsVisible(boolean visible) {
        visibleLabel.setVisible(visible);
        donnaVisibleToggleButton.setVisible(visible);
        uomoVisibleToggleButton.setVisible(visible);
    }

    @FXML
    public void registerButtonOnAction(ActionEvent event) {
        // 1) Costruisci la bean dai campi UI
        RegistrationBean bean = new RegistrationBean();
        bean.setFirstName(safeText(nomeTextField));
        bean.setLastName(safeText(cognomeTextField));
        bean.setEmail(safeText(emailTextField));
        bean.setPhoneNumber(safeText(telefonoTextField));
        bean.setPassword(safeText(setPasswordField));
        bean.setRepeatPassword(safeText(confirmPassword));
        bean.setUserType(barberCheckBox.isSelected() ? Role.BARBIERE : Role.CLIENTE);

        // 2) Se è barbiere, obbliga la scelta Uomo/Donna e salva nella bean
        if (barberCheckBox.isSelected()) {
            if (genderGroup == null || genderGroup.getSelectedToggle() == null) {
                showAlert(Alert.AlertType.ERROR, "Se sei parrucchiere devi selezionare Uomo o Donna.");
                return;
            }
            if (uomoVisibleToggleButton.isSelected()) {
                bean.setSpecializzazione(GenderCategory.UOMO);
            } else if (donnaVisibleToggleButton.isSelected()) {
                bean.setSpecializzazione(GenderCategory.DONNA);
            }
        }

        // 3) Validazione lato bean
        List<String> errors = bean.validate();
        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, String.join("\n", errors));
            return;
        }

        // 4) Chiamata al controller applicativo
        String esito = registrazioneController.register(bean);

        // 5) Gestione esito
        switch (esito) {
            case "success" -> {
                showAlert(Alert.AlertType.INFORMATION, "Registrazione completata!");
                goToLogin();
            }
            case "error:email_exists" -> showAlert(Alert.AlertType.ERROR, "Email già registrata.");
            case "error:phone_exists" -> showAlert(Alert.AlertType.ERROR, "Telefono già registrato.");
            case "error:validation" -> showAlert(Alert.AlertType.ERROR, "Dati non validi. Controlla i campi.");
            case "error:conflict" -> showAlert(Alert.AlertType.ERROR, "Conflitto dati (email/telefono).");
            default -> showAlert(Alert.AlertType.ERROR, "Errore inatteso durante la registrazione.");
        }
    }

    @FXML
    public void loginHyperlinkOnAction(ActionEvent event) {
        goToLogin();
    }

    private void goToLogin() {
        try {
            WindowManager.getInstance().switchScene("LoginView.fxml","ZAC ZAC");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile aprire la schermata di login.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Errore" : "Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safeText(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }
}
