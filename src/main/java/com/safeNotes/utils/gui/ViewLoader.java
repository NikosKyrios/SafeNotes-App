package com.safeNotes.utils.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality; //controls if a window blocks another one
import javafx.stage.Stage;
import java.io.IOException;

import javax.management.RuntimeErrorException; 

public class ViewLoader {
    public static Scene loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            return new Scene(root);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public static <T> T loadFXML(String fxmlPath, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();
            return controller;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public static Stage createModalStage(String title, String fxmlPath) {
        Stage modalStage = new Stage();
        modalStage.setTitle(title);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        loadFXML(fxmlPath, modalStage);
        modalStage.setResizable(false);
        return modalStage;
    }
}
