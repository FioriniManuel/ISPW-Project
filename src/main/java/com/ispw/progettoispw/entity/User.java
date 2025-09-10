package com.ispw.progettoispw.model.entity;


import com.ispw.progettoispw.Enum.Role;

public abstract class User {
     private String email;
     private String nome;
     private String cognome;
     private String numerodiTelefono;
     private String password;
     private String id;

     protected User(){ }

     protected User (String nome,String email, String cognome, String numerodiTelefono,String password, String id){
         this.nome= nome;
         this.cognome=cognome;
         this.numerodiTelefono=numerodiTelefono;
         this.email=email;
         this.password=password;
         this.id=id;

     }

     //Getters/setters comuni
    public String getFirstName(){return nome;}
    public String getLastName(){return cognome;}
    public String getphoneNumber(){return numerodiTelefono;}
    public String getEmail(){return email;}
    public String getPassword(){return password;}
    public String getId(){ return id;}

    public void setFirstName(String nome) {
        this.nome = nome;
    }
    public void setLastName(String cognome) {
        this.cognome = cognome;
    }
    public void setphoneNumber(String numerodiTelefono) {
        this.numerodiTelefono = numerodiTelefono;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setId(String id){ this.id=id;}

    public abstract Role getRole();
}
