package com.ispw.progettoispw.pattern;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import javafx.scene.Scene;
import javafx.scene.Parent;


public class WindowManager {
    private static WindowManager instance;
    private Stage primaryStage;

    private WindowManager(Stage stage) {
        this.primaryStage = stage;


    }

    public static WindowManager getInstance(Stage stage) {
        if (instance == null) {
            instance = new WindowManager(stage);
        }
        return instance;
    }

    public static WindowManager getInstance(){
        return instance;
    }

    public void switchScene(String fxmlFile, String nome) throws IOException {
        // Usa path assoluto
        URL fxmlLocation = getClass().getResource("/com/ispw/progettoispw/" + fxmlFile);
        if (fxmlLocation == null) {
            throw new IOException("FXML non trovato: " + fxmlFile);
        }
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(nome);
        primaryStage.show();
        primaryStage.setOnShown(e -> primaryStage.centerOnScreen());
    }
    public <T> T switchScene(String fxmlFile, String title, Class<T> controllerClass, Consumer<T> initializer) throws IOException {
        URL fxmlLocation = getClass().getResource("/com/ispw/progettoispw/" + fxmlFile);
        if (fxmlLocation == null) {
            throw new IOException("FXML non trovato: " + fxmlFile);
        }
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controllerClass != null && !controllerClass.isInstance(controller)) {
            throw new IllegalStateException("Controller atteso " + controllerClass.getName()
                    + " ma trovato " + (controller == null ? "null" : controller.getClass().getName()));
        }
        @SuppressWarnings("unchecked")
        T typedController = (T) controller;

        if (initializer != null && typedController != null) {
            initializer.accept(typedController);
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
        primaryStage.setOnShown(e -> primaryStage.centerOnScreen());

        return typedController;
    }
}