package com.ispw.progettoispw;

import com.ispw.progettoispw.pattern.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        WindowManager.getInstance(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Impostazioni Iniziali");
        stage.setScene(scene);

        stage.setMinWidth(400);
        stage.setMinHeight(300);
        stage.setResizable(true); // default è true, ma meglio esplicitarlo

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}