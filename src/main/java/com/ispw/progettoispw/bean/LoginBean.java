package com.ispw.progettoispw.bean;

import com.ispw.progettoispw.Enum.Role;

public class LoginBean {
    private String email;
    private String password;
    private Role userType; // opzionale: CLIENTE/BARBIERE; può essere null

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getUserType() { return userType; }
    public void setUserType(Role userType) { this.userType = userType; }
}
