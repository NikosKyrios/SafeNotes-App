package com.safeNotes.controllers.auth;

import com.safeNotes.app.SafeNotesApp;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.services.auth.AuthenticationService;
import com.safeNotes.services.auth.AuthenticationServiceImpl;
import com.safeNotes.repositories.SQLUserRepository;
import com.safeNotes.services.encryption.Argon2Hasher;
import com.safeNotes.utils.gui.AlertHelper;
import com.safeNotes.services.auth.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ProgressIndicator typingProgress;
    @FXML private CheckBox rememberMe;
    @FXML private HBox keystrokeIndicator;
    @FXML private VBox securityStatusBox;
    @FXML private Label securityStatusLabel;
    @FXML private ProgressBar securityProgress;

    private long passwordStartTime;
    private int loginAttempts;
    private AuthenticationService authService;

    @FXML
    public void initialize() {
        try {
            keystrokeMonitoring();
            authService = new AuthenticationServiceImpl(new SQLUserRepository(), new Argon2Hasher(), SessionManager.getInstance()); 
            InputValidation();
        }
        catch (StorageException e) {
            AlertHelper.showError("Failed to initialize database: " + e.getMessage());
        }

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
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 4) {
                usernameField.setStyle("-fx-border-color: #ff4444;");
            }
            else {
                usernameField.setStyle("");
            }
        });
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showError("Please enter both username and password");
            return;
        }

        try {
            var result = authService.login(username, password);

            if (result.isSuccess()) {

                securityStatusBox.setVisible(true);
                securityStatusLabel.setText("Login successful");
                securityProgress.setProgress(1.0);
                showDashboard();
            }
            else {
                loginAttempts++;
                AlertHelper.showError(result.getMessage());

                if (loginAttempts >= 3) {
                    securityStatusBox.setVisible(true);
                    securityStatusLabel.setText("Multiple failed attempts - Security alert");
                    securityProgress.setProgress(0.3);
                }
            }
        } 
        catch (Exception e) {
            AlertHelper.showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
        passwordField.clear();
    }

    @FXML
    private void onRegister() {
        SafeNotesApp.getInstance().showRegisterScreen();
    }

    @FXML
    private void onForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText(null);
        alert.showAndWait();
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

}
