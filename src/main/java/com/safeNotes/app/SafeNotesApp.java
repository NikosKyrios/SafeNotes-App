package com.safeNotes.app;

import com.safeNotes.config.AppConstants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import com.safeNotes.utils.gui.ViewLoader;

public class SafeNotesApp extends Application {
    private Stage primaryStage;
    private static SafeNotesApp instance; 

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        setupPrimaryStage();
        showLoginScreen();
    }

    private void setupPrimaryStage() {
        primaryStage.setTitle(AppConstants.APP_TITLE + " v" + AppConstants.APP_VERSION);
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/main/recources/images/icons/lock.png"))); // because resources in in jar files
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        primaryStage.setAlwaysOnTop(true); //prevent from screenshots (WEAK!!!)
        
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            handleExit();
        });
    }

    public void showLoginScreen() {
        try {
            Scene loginScene = ViewLoader.loadScene("/com/safeNotes/views/fxml/auth/login.fxml");
            primaryStage.setScene(loginScene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } 
        catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showDashboard() {
        try {
            Scene dashboardScene = ViewLoader.loadScene("/com/safeNotes/views/fxml/dashboard/dashboard.fxml");
            primaryStage.setScene(dashboardScene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
           System.err.println("Error loading dashboard screen: " + e.getMessage());
           e.printStackTrace();
        }
    }

    private void handleExit() {
        // TODO: Clear all sensitive data from memory
        // TODO: Close all encrypted notes
        // TODO: Log logout event
        System.exit(0);
    }

    public static SafeNotesApp getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
