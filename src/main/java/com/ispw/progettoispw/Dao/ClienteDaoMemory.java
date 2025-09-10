package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.Cliente;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteDaoMemory implements GenericDao<Cliente> {

    private final Map<String, Cliente> storage = new ConcurrentHashMap<>();

    @Override
    public void create(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("Cliente null");

        if (cliente.getId() == null || cliente.getId().isEmpty()) {
            cliente.setId(UUID.randomUUID().toString());
        }

        if (findByEmail(cliente.getEmail()) != null) {
            throw new IllegalStateException("Email già usata");
        }
        if (findByPhone(cliente.getphoneNumber()) != null) {
            throw new IllegalStateException("Telefono già usato");
        }

        storage.put(cliente.getId(), cliente);
    }


    @Override
    public Cliente read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida per la ricerca del Client");
        }

        Cliente cliente= storage.get(id);
        if (cliente == null){
            throw new NoSuchElementException("Cliente non trovato: id=" + id);
        }
        return cliente;
    }

    @Override
    public void update(Cliente cliente) {

    if(cliente==null || cliente.getId()==null) throw new IllegalArgumentException("Cliente/ Id null");

        Cliente old = storage.get(cliente.getId());
        if (old == null) throw new NoSuchElementException("Cliente inesistente: " + cliente.getId());

     Cliente existingPhone= findByPhone(cliente.getphoneNumber());
     if(existingPhone !=null && ! existingPhone.getId().equals(cliente.getId())){
         throw new IllegalStateException("Telefono già usato");
     }
        Cliente existingEmail= findByEmail(cliente.getEmail());
        if(existingEmail !=null && ! existingEmail.getId().equals(cliente.getId())){
            throw new IllegalStateException("Email già usata");
        }
     storage.put(cliente.getId(), cliente);
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida per la ricerca del Client");
        }
        storage.remove(id);

    }

    @Override
    public List<Cliente> readAll() {
        return new ArrayList<>(storage.values());
    }



   public Cliente findByEmail(String email){
        if(email==null)return null;
        String norm= email.trim().toLowerCase();
        return storage.values().stream().filter(cliente -> cliente.getEmail()!=null && norm.equals(cliente.getEmail().trim().toLowerCase())).findFirst().orElse(null);}


    public Cliente findByPhone(String phone){
        if(phone==null)return null;
        String norm= phone.replaceAll("\\s+","");
        return storage.values().stream().filter(cliente -> cliente.getphoneNumber()!=null && norm.equals(cliente.getphoneNumber().replaceAll("\\s+",""))).findFirst().orElse(null);}

}
