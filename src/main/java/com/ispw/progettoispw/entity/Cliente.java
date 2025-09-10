package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.Enum.Role;

public class Cliente extends User{



    public Cliente() {}
    public Cliente(String nome,String email, String cognome, String numerodiTelefono,String password,String ClienteId) {
        super(nome, email, cognome, numerodiTelefono, password,ClienteId);
    }

    @Override
    public Role getRole() {
        return Role.CLIENTE;
    }
}
