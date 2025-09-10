package com.ispw.progettoispw.bean;

import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bean del form di registrazione.
 * Contiene i dati comuni e specifici (Cliente/Barbiere),
 * più un metodo validate() per controlli di base.
 */
public class RegistrationBean {

    // Tipo utente scelto: "client" oppure "barber"
    private Role userType;
    private static final Pattern IT_MOBILE_RX = Pattern.compile("^3\\d{9}$");
    // Dati comuni
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Credenziali
    private String password;
    private String repeatPassword;

    // Dati specifici per Barbiere
    private GenderCategory specializzazione;
    private Boolean active = Boolean.TRUE;

    // ------------------ Getters & Setters ------------------

    public Role getUserType() { return userType; }
    public void setUserType(Role userType) { this.userType = userType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = trim(firstName); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = trim(lastName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = trim(email); }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = normalizePhone(phoneNumber); }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRepeatPassword() { return repeatPassword; }
    public void setRepeatPassword(String repeatPassword) { this.repeatPassword = repeatPassword; }

    public GenderCategory getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(GenderCategory specializzazione) { this.specializzazione = specializzazione; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    // ------------------ Utilità ruolo ------------------

    public boolean isClient() {
        return Role.CLIENTE.equals(userType);
    }

    public boolean isBarber() {
        return Role.BARBIERE.equals(userType);
    }

    // ------------------ Validazione ------------------

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[\\w-.]+@[\\w-]+\\.[A-Za-z]{2,}$");

    /**
     * Ritorna una lista di errori. Vuota se tutto ok.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (isBlank(firstName)) errors.add("Nome obbligatorio");
        if (isBlank(lastName))  errors.add("Cognome obbligatorio");

        if (isBlank(email) || !EMAIL_RX.matcher(email).matches())
            errors.add("Email non valida");

        if (isBlank(phoneNumber)){
            errors.add("Telefono obbligatorio");}
        else{ String norm=normalizePhone( phoneNumber);
            if (!IT_MOBILE_RX.matcher(norm).matches()){
                errors.add("Telefono non valido.Inserisci 10 cifre Italiane.");}
                else { this.phoneNumber=norm;}
            }
        if (isBlank(password) || password.length() < 8 || password.length() > 16)
            errors.add("Password tra 8 e 16 caratteri");

        if (!safeEquals(password, repeatPassword))
            errors.add("Le password non coincidono");

        if (!(isClient() || isBarber()))
            errors.add("Tipo utente non valido");

        if (isBarber()) {
            if (specializzazione==null)
                errors.add("Specializzazione obbligatoria per i barbieri");
            if (active == null) active = Boolean.TRUE; // default
        }

        return errors;
    }

    // ------------------ Helpers ------------------

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static boolean safeEquals(String a, String b) { return a == null ? b == null : a.equals(b); }
    private static String trim(String s) { return s == null ? null : s.trim(); }

    /** Normalizza numero: rimuove spazi, -, ( ), e prefisso +39/0039 */
    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[\\s\\-()]", "");
        if (p.startsWith("+39"))  p = p.substring(3);
        if (p.startsWith("0039")) p = p.substring(4);
        return p;
    }
}
