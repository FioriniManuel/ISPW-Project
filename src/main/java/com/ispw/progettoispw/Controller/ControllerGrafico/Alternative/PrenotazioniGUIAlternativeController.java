package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class PrenotazioniGUIAlternativeController {

    @FXML private ListView<ServizioBean> uomoList;
    @FXML private ListView<ServizioBean> donnaList;
    @FXML private ComboBox<BarbiereBean> barberCombo;

    private final BookingController booking = new BookingController();

    /** Stato selezione corrente (toggle) */
    private String selectedServiceId = null;
    private GenderCategory selectedGender = null;

    @FXML
    public void initialize() {
        // Popola liste (VM) da seed
        uomoList.setItems(FXCollections.observableArrayList(
                booking.listServiziByCategoryVM(GenderCategory.UOMO)
        ));
        donnaList.setItems(FXCollections.observableArrayList(
                booking.listServiziByCategoryVM(GenderCategory.DONNA)
        ));
        uomoList.setPlaceholder(new Label("Nessun servizio uomo disponibile."));
        donnaList.setPlaceholder(new Label("Nessun servizio donna disponibile."));

        // Celle con pulsante "Conferma" (toggle-on/ off) — lavorano con ServizioBean
        uomoList.setCellFactory(lv -> new ServiceCell());
        donnaList.setCellFactory(lv -> new ServiceCell());

        // Combo barbieri: mostra il displayName (già pronto nella VM)
        barberCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(BarbiereBean b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : b.getDisplayName());
            }
        });
        barberCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(BarbiereBean b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : b.getDisplayName());
            }
        });
        barberCombo.setPromptText("Seleziona un barbiere");
        barberCombo.setDisable(true);
    }

    /** Rende coerente lo stato di abilitazione delle celle dopo una selezione/deselezione. */
    private void refreshLists() {
        uomoList.refresh();
        donnaList.refresh();
    }

    /** Carica barbieri SOLO in base al genere del servizio selezionato (VM). */
    private void loadBarbersBySelectedGender() {
        if (selectedGender == null) {
            barberCombo.getItems().clear();
            barberCombo.setDisable(true);
            return;
        }
        List<BarbiereBean> barbers = booking.listBarbersByGenderVM(selectedGender);
        barberCombo.getItems().setAll(barbers);
        boolean empty = barbers.isEmpty();
        barberCombo.setDisable(empty);
        barberCombo.setPromptText(empty ? "Nessun barbiere per questa specializzazione" : "Seleziona un barbiere");
        if (!empty) barberCombo.getSelectionModel().clearSelection();
    }

    /** Cella custom: etichetta servizio + pulsante "Conferma" (toggle) per ServizioBean. */
    private class ServiceCell extends ListCell<ServizioBean> {
        private final Label label = new Label();
        private final Button confirmBtn = new Button("Conferma");
        private final HBox box = new HBox(12, label, confirmBtn);

        ServiceCell() {
            confirmBtn.setOnAction(e -> {
                ServizioBean s = getItem();
                if (s == null) return;

                // Toggle: se riclicco lo stesso servizio -> deselezione
                if (s.getId().equals(selectedServiceId)) {
                    selectedServiceId = null;
                    selectedGender = null;
                    loadBarbersBySelectedGender();
                    refreshLists();
                    return;
                }

                // Nuova selezione
                selectedServiceId = s.getId();
                // Ricaviamo il gender dal servizio originale tramite ID (oppure tienilo in ServizioBean se preferisci)
                // Se vuoi evitare accessi all'entity, puoi estendere ServizioBean per includere la categoria.
                // Qui usiamo getServizioVM per evitare entity in GUI.
                ServizioBean vm = booking.getServizioVM(s.getId());
                if (vm != null) {
                    // NOTA: se vuoi evitare qualunque accesso all'entity, aggiungi 'GenderCategory category' a ServizioBean
                    // e setta selectedGender lì. In mancanza, teniamo selectedGender dal click list originario.
                    // In questo esempio, deduciamo il gender dalla lista in cui l'utente ha cliccato:
                    selectedGender = (getListView() == uomoList) ? GenderCategory.UOMO : GenderCategory.DONNA;
                }

                loadBarbersBySelectedGender();
                refreshLists();
            });
        }

        @Override
        protected void updateItem(ServizioBean s, boolean empty) {
            super.updateItem(s, empty);
            if (empty || s == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            // Etichetta: nome + prezzo (via BookingController helper per VM)
            label.setText(booking.buildServiceLabel(s));

            // Se c'è un servizio selezionato, disabilita il pulsante degli altri
            boolean otherSelected = selectedServiceId != null && !s.getId().equals(selectedServiceId);
            confirmBtn.setDisable(otherSelected);
            box.setOpacity(otherSelected ? 0.55 : 1.0);

            setText(null);
            setGraphic(box);
        }
    }

    /* ===== Bottoni ===== */

    @FXML
    private void onBack() {
        try {
            WindowManager.getInstance().switchScene("HomeViewAlternative.fxml", "Home");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossibile tornare alla Home.").showAndWait();
        }
    }

    @FXML
    private void onContinua() {
        if (selectedServiceId == null) {
            new Alert(Alert.AlertType.WARNING, "Seleziona un servizio e confermalo.").showAndWait();
            return;
        }
        BarbiereBean selectedBarber = barberCombo.getSelectionModel().getSelectedItem();
        if (selectedBarber == null) {
            new Alert(Alert.AlertType.WARNING, "Seleziona un professionista.").showAndWait();
            return;
        }

        // Prepara la BookingBean per lo step successivo
        BookingBean bean = new BookingBean();
        bean.setServiziId(selectedServiceId);
        bean.setBarbiereDisplay(selectedBarber.getDisplayName());
        // Compatibilità con il tuo BookingController: usi "Nome Cognome" come id del barbiere
        bean.setBarbiereId(selectedBarber.getId());
        bean.setCategoria(selectedGender);

        // Dati servizio (nome/prezzo/durata) usando SOLO la VM
        ServizioBean sVM = booking.getServizioVM(selectedServiceId);
        if (sVM != null) {
            bean.setServiceName(sVM.getName());
            bean.setDurataTotaleMin(sVM.getDurationMin());
            BigDecimal price = (sVM.getPrice() == null) ? BigDecimal.ZERO : sVM.getPrice();
            bean.setPrezzoTotale(price);
        }

        // Cliente corrente (se disponibile)
        String clientId = LoginController.getId();
        if (clientId != null && !clientId.isBlank()) {
            bean.setClienteId(clientId);
        }

        // Salva in "sessione" applicativa
        booking.saveBookingToSession(bean);

        // Vai alla schermata successiva
        try {
            WindowManager.getInstance().switchScene("OrarioViewAlternative.fxml", "Scegli Orario");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossibile aprire la schermata Orario.").showAndWait();
        }
    }
}
