package com.ispw.progettoispw.Session;

import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Enum.StorageOption;

import java.util.Objects;

public final class Session {


    private final String phoneNumber;
    private final String id;           // es. UUID/ID DB
    private final String email;
    private final String displayName;  // es. "Mario Rossi"
    private final Role role;


    public Session(String phoneNumber, String id, String email, String displayName, Role role) {
        this.phoneNumber = phoneNumber;
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.role = Objects.requireNonNull(role, "role");

    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
