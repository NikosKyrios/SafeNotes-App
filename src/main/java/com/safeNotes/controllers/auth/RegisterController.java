package com.safeNotes.controllers.auth;

import java.util.ArrayList;
import java.util.List;

import com.safeNotes.config.AppConstants;
import com.safeNotes.exceptions.AuthException;
import com.safeNotes.models.domain.TypingData;
import com.safeNotes.models.dto.RegistrationRequest;
import com.safeNotes.models.dto.RegistrationResult;
import com.safeNotes.services.auth.AuthenticationService;
import com.safeNotes.utils.gui.AlertHelper;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterController {

    //password requirements
    @FXML private Label lengthReq;
    @FXML private Label upperReq;
    @FXML private Label lowerReq;
    @FXML private Label digitReq;
    @FXML private Label specialReq;

    //errors
    @FXML private Label usernameError;
    @FXML private Label passwordMatchError;
    @FXML private Label termsError;

    //fields & boxes
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox enableKeyStrokeCkeck;
    @FXML private CheckBox enableTwoFactorCheck;
    @FXML private CheckBox enableAutoLockCheck;
    @FXML private CheckBox termsCheck;

    //password strength
    @FXML private VBox passwordStrengthBox;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;


    private AuthenticationService authenticationService;
    private List<Long> typingSamples = new ArrayList<>();
    private long typingStartTime;
    private int sampleCount = 0;

    @FXML
    public void initialize() {
        setupPasswordValidation();
        setupUsernameValidation();
    }

    private void setupPasswordValidation() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword(newVal);
            checkPasswordMatch();
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordMatch();
        });
    }

    private void validatePassword(String password) {
        //check requirements
        boolean lengthOk = password.length() >= 10;
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        //update requirements
        updateRequirementLabel(lengthReq, lengthOk, "✓ At least 10 charachters");
        updateRequirementLabel(upperReq, hasUpper, "✓ Upperace letter");
        updateRequirementLabel(lowerReq, hasLower, "✓ LowerCase letter");
        updateRequirementLabel(digitReq, hasDigit, "✓ Number");
        updateRequirementLabel(specialReq, hasSpecial, "✓ Special charachter");

        //calculate strength score
        int score = 0;
        if (lengthOk) score += 20;
        if (hasUpper) score += 20;
        if (hasLower) score += 20;
        if (hasDigit) score += 20;
        if (hasSpecial) score += 20;

        //update strength bar/progress
        double progress = score / 100.0;
        passwordStrengthBar.setProgress(progress);

        //update label/color
        if (score < 40) {
            passwordStrengthLabel.setText("Very weak");
            passwordStrengthLabel.getStyleClass().setAll("strength-very-weak");
        }
        else if (score < 60) {
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.getStyleClass().setAll("strength-weak");
        }
        else if (score < 80) {
            passwordStrengthLabel.setText("Good");
            passwordStrengthLabel.getStyleClass().setAll("strength-good");
        }
        else {
            passwordStrengthLabel.setText("Strong");
            passwordStrengthLabel.getStyleClass().setAll("strength-strong");
        }
        passwordStrengthBox.setVisible(password.length() > 0);
    }

    private void updateRequirementLabel(Label label, boolean met, String text) {
        label.setText(text);

        if (met) {
            label.getStyleClass().remove("requirement-unmet");
            label.getStyleClass().add("requirement-met");
        }
        else {
            label.getStyleClass().remove("requirement-met");
            label.getStyleClass().add("requirement-unmet");
        }
    }

    private void checkPasswordMatch() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (confirm.isEmpty()) {
            passwordMatchError.setVisible(false);
        }
        else if (!password.equals(confirm)) {
            passwordMatchError.setVisible(true);
        }
        else {
            passwordMatchError.setVisible(false);
        }
    }

    private void setupUsernameValidation() {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 3) {
                usernameError.setText("Username must be at least 3 charachters");
                usernameError.setVisible(true);
            }
            else if (newVal.length() > 20) {
                usernameError.setText("Username must be maximum 20 charachters");
                usernameError.setVisible(true);
            }
            else {
                usernameError.setVisible(false);
            }
        });
    }

    @FXML
    private void onRegister() {
        if (!validateForm()) return;

        //Collect samples for keyStroke
        List<TypingData> typingSamples = null;
        if (enableKeyStrokeCkeck.isSelected()) {
            typingSamples = collectTypingSamples();
            if (typingSamples == null || typingSamples.size() < 2) {
                AlertHelper.showError("Type your password 2 times for keyStroke");
                return;
            }
        }

        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(usernameField.getText().trim());
        request.setPassword(passwordField.getText());
        request.setEnableKeyStroke(enableKeyStrokeCkeck.isSelected());
        request.setEnable2FA(enableTwoFactorCheck.isSelected());
        request.setEnableLocationCheck(false);
        request.setAutoLockEnabled(enableAutoLockCheck.isSelected());
        request.setAutoLockMins(15);
        request.setTypingSamples(typingSamples);

        try {
            RegistrationResult result = authenticationService.register(request);

            if (result.isSuccess()) {
                AlertHelper.showSuccess("Registration successfull!");
                onCancel();
            }
            else {
                AlertHelper.showError(result.getMessage());
            }
        }
        catch (AuthException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        //To do: return to login page
    }

    private boolean validateForm() {
        boolean valid = true;

        if (usernameField.getText().trim().length() < 3) {
            usernameError.setVisible(true);
            valid = false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            passwordMatchError.setVisible(true);
            valid = false;
        }

        if (!termsCheck.isSelected()) {
            termsError.setText("You must accept the terms and conditions");
            termsError.setVisible(true);
            valid = false;
        }

        return valid;
    }

    private void setupKeyStroke() {
        passwordField.setOnKeyPressed(event -> {
            if (passwordField.getText().isEmpty()) {
                typingStartTime = System.currentTimeMillis();
            }
        });

        passwordField.setOnKeyReleased(event -> {
            String password = passwordField.getText();
            if (!password.isEmpty() && sampleCount < AppConstants.KEYSTROKE_SAMPLES_REQUIRED) {
                long elapsed = System.currentTimeMillis() - typingStartTime;
                typingSamples.add(elapsed);
                sampleCount++;

                if (sampleCount == AppConstants.KEYSTROKE_SAMPLES_REQUIRED) {
                    System.out.println("KeyStroke samples collected: " + typingSamples);
                }
            }
        });
    }

    private List<TypingData> collectTypingSamples() {
        List<TypingData> samples = new ArrayList<>();
        for (Long time : typingSamples) {
            TypingData data = new TypingData();
            data.setTotalTypingTime(time);
            data.setIsCopyPaste(time < 500);
            samples.add(data);
        }
        return samples;
    }
}
