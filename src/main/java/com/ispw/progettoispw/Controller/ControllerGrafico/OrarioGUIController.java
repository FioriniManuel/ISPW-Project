package com.ispw.progettoispw.ControllerGrafico;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

public class OrarioGUIController {

    public void initialize(){

    }

    public void listinoButtonOnAction(ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("PrenotazioneView.fxml","Listino");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }

    public void congratulazioniButtonOnAction(ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("CongratulazioniView.fxml","Congratulazioni");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }

    public void pagamentoButtonOnAction(ActionEvent event){
        try{
            WindowManager.getInstance().switchScene("PagamentoView.fxml","Pagamento");
        } catch( IOException e){
            throw new RuntimeException(e);
        }
    }
}
