package com.ispw.progettoispw.Dao;

import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.Enum.GenderCategory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BarbiereDaoMemory implements GenericDao<Barbiere> {

   private final Map<String, Barbiere> byId = new ConcurrentHashMap<>();


   public void create(Barbiere b) {
      if (b == null) throw new IllegalArgumentException("Barbiere null");
      if (b.getId() == null || b.getId().isEmpty()) {
         b.setId(UUID.randomUUID().toString());
      }
      // (opz) vincoli: email e telefono unici
      if (b.getEmail() != null && findByEmail(b.getEmail()) != null) {
         throw new IllegalStateException("Email già registrata");
      }
      if (b.getphoneNumber() != null && findByPhone(b.getphoneNumber()) != null) {
         throw new IllegalStateException("Telefono già registrato");
      }
      byId.put(b.getId(), b);
   }

   @Override
   public Barbiere read(Object... keys) {
      if (keys.length != 1 || !(keys[0] instanceof String id)) {
         throw new IllegalArgumentException("Chiave id non valida");
      }
      Barbiere b = byId.get(id);
      if (b == null) throw new NoSuchElementException("Barbiere non trovato: " + id);
      return b;
   }

   @Override
   public void update(Barbiere b) {
      if (b == null || b.getId() == null) throw new IllegalArgumentException("Barbiere/id null");
      Barbiere old = byId.get(b.getId());
      if (old == null) throw new NoSuchElementException("Barbiere inesistente: " + b.getId());

      // (opz) ricontrollo unicità email/telefono escludendo se stesso
      Barbiere byEmail = (b.getEmail() == null) ? null : findByEmail(b.getEmail());
      if (byEmail != null && !byEmail.getId().equals(b.getId())) {
         throw new IllegalStateException("Email già usata da un altro barbiere");
      }
      Barbiere byPhone = (b.getphoneNumber() == null) ? null : findByPhone(b.getphoneNumber());
      if (byPhone != null && !byPhone.getId().equals(b.getId())) {
         throw new IllegalStateException("Telefono già usato da un altro barbiere");
      }

      byId.put(b.getId(), b);
   }

   @Override
   public void delete(Object... keys) {
      if (keys.length != 1 || !(keys[0] instanceof String id)) {
         throw new IllegalArgumentException("Chiave id non valida");
      }
      byId.remove(id);
   }

   @Override
   public List<Barbiere> readAll() {
      return new ArrayList<>(byId.values());
   }

   /* ================== Query extra ================== */


   public Barbiere findByEmail(String email) {
      if (email == null) return null;
      String norm = email.trim().toLowerCase();
      return byId.values().stream()
              .filter(b -> b.getEmail() != null &&
                      norm.equals(b.getEmail().trim().toLowerCase()))
              .findFirst()
              .orElse(null);
   }


   public Barbiere findByPhone(String phone) {
      if (phone == null) return null;
      String norm = normalizePhone(phone);
      return byId.values().stream()
              .filter(b -> b.getphoneNumber() != null &&
                      norm.equals(normalizePhone(b.getphoneNumber())))
              .findFirst()
              .orElse(null);
   }


   public List<Barbiere> findBySpecialty(GenderCategory specialty) {
      if (specialty == null) return readAll();
      return byId.values().stream()
              .filter(b -> specialty.equals(b.getSpecializzazione()))
              .collect(Collectors.toList());
   }



   /* ================== Helpers ================== */

   // Normalizza numeri: rimuove spazi, -, ( ), e prefisso +39 opzionale
   private String normalizePhone(String phone) {
      String p = phone.replaceAll("\\s+", "");

      return p;
   }}