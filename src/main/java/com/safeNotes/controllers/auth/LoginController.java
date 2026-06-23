package com.safeNotes.controllers.auth;

import com.safeNotes.app.SafeNotesApp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.*;

public class LoginController {
    @FXML private TextField usernamField;
    @FXML private PasswordField passwordField;
    @FXML private ProgressIndicator typingProgress;
    @FXML private CheckBox rememberMe;
    @FXML private HBox keystrokeIndicator;
    @FXML private VBox securityStatusBox;
    @FXML private Label securityStatusLabel;
    @FXML private ProgressBar securityProgress;

    private long passwordStartTime;
    private int loginAttempts;

    @FXML
    public void initialize() {
        keystrokeMonitoring();
    } 
    
    private void keystrokeMonitoring() {
        passwordField.setOnKeyPressed(event -> {
            if (passwordField.getText().isEmpty()) {
                passwordStartTime = System.currentTimeMillis();
                keystrokeIndicator.setVisible(false);
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                long elapsed = System.currentTimeMillis() - passwordStartTime;
                double targetTime = passwordField.getText().length() * 500;
                double progress = Math.min(elapsed / targetTime, 1.0);
                typingProgress.setProgress(progress);
            }
        });
    }

    private void InputValidation() {
        usernamField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 4) {
                usernamField.setStyle("-fx-border-color: #ff4444;");
            }
            else {
                usernamField.setStyle("");
            }
        });
    }

    @FXML
    private void onLogin() {
        String username = usernamField.getText().trim();
        String password = passwordField.getText();

        //ToDO authentication with database
        System.out.println("Login tried by: " + username);

        if (username.equals("demo") && password.equals("demo")) {
            showDashboard();
        }
        else {
            showError("Either username or password is wrong");
            loginAttempts++;

            //to alert that someone is trying to many times
            if (loginAttempts >= 2) {
                securityStatusBox.setVisible(true);
                securityStatusLabel.setText("Multiple failed attempts detected");
                securityProgress.setProgress(0.8);
            }
        }
        passwordField.clear();
    }

    @FXML
    private void onRegister() {
        //TO DO
        System.out.println("Nice");
    }

    @FXML
    private void onForgotPassword() {
        //TO DO
        System.out.println("Nice");
    }

    private void showDashboard() {
        try {
           SafeNotesApp app = SafeNotesApp.getInstance();
           app.showDashboard();

        } 
        catch (Exception e) {
            System.err.println("Error navigating to dashboard" + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
