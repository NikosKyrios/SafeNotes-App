package com.safeNotes.controllers.notes;

import java.net.URL;
import java.util.ResourceBundle;

import com.safeNotes.app.SafeNotesApp;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.models.domain.User;
import com.safeNotes.repositories.SQLNoteRepository;
import com.safeNotes.services.auth.SessionManager;
import com.safeNotes.services.encryption.AESEncryptionService;
import com.safeNotes.services.encryption.Argon2Hasher;
import com.safeNotes.services.notes.NoteService;
import com.safeNotes.services.notes.NoteServiceImpl;
import com.safeNotes.utils.gui.AlertHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class NoteEditorController implements Initializable {
    
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private CheckBox lockCheck;
    @FXML private CheckBox blurCheck;
    @FXML private Button setPinButton;
    @FXML private ComboBox<String> securityLevelCombo;
    @FXML private Label pinStatusLabel;

    private NoteService noteService;
    private User currentUser;
    private SecureNote currentNote;
    private String currentPin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
                        
            byte[] salt = new AESEncryptionService().generateSalt();
            byte[] key = new AESEncryptionService().generateKey(SessionManager.getInstance().getCurrentUser().getUserId(), salt);
            
            noteService = new NoteServiceImpl(new SQLNoteRepository(), new Argon2Hasher(), new AESEncryptionService(), key);
            currentUser = SessionManager.getInstance().getCurrentUser();

            setupSecurityLevels();
            setupListeners();
        }
        catch (Exception e) {
            AlertHelper.showError("Failed to initialize editor: " + e.getMessage());
        }
    }

    private void setupSecurityLevels() {
        securityLevelCombo.getItems().addAll("LOW", "MEDIUM", "HIGH");
        securityLevelCombo.setValue("LOW");
    }

    private void setupListeners() {
        lockCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setPinButton.setVisible(newVal);
            if (newVal && currentNote != null && currentNote.getPin() !=null) {
                pinStatusLabel.setText("Pin is set");
                pinStatusLabel.setVisible(true);
            }
            else if (!newVal) {
                pinStatusLabel.setVisible(false);
            }
        });

        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                titleField.setStyle("-fx-border-color: #e74c3c;");
            }
            else {
                titleField.setStyle("");
            }
        });
    }

    public void loadNote(SecureNote note) {
        this.currentNote = note;

        if (note != null) {
            titleField.setText(note.getTitle());
            contentArea.setText(note.getContent());
            lockCheck.setSelected(note.isLocked());
            blurCheck.setSelected(note.isBlurred());
            securityLevelCombo.setValue(note.getSecurityLevel());

            if (note.getPin() != null) {
                pinStatusLabel.setText("Pin is set");
                pinStatusLabel.setVisible(true);
                setPinButton.setText("Change Pin");
            }

            setPinButton.setVisible(note.isLocked());
        }
    }

    @FXML
    private void onSave() {
        String title = titleField.getText().trim();
        String content = contentArea.getText();

        if (title.isEmpty()) {
            AlertHelper.showError("Please enter a title");
            titleField.requestFocus();
            return;
        }

        try {
            if (currentNote == null) {
                SecureNote newNote = noteService.createNote(title, content, currentUser.getUserId());
                applySecuritySettings(newNote);
                AlertHelper.showSuccess("Note created");
            }
            else {
                noteService.updateNote(currentNote.getId(), title, content, currentUser.getUserId());

                applySecuritySettings(currentNote);
                AlertHelper.showSuccess("Note updated");
            }

        }
        catch (Exception e) {
            AlertHelper.showError("Failed to save note: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applySecuritySettings(SecureNote note) throws Exception {
        if (lockCheck.isSelected()) {
            if (currentPin == null && note.getPin() == null) {
                return;
            }
            String pinForUse = currentPin != null ? currentPin : note.getPin();
            noteService.lockNote(note.getId(), pinForUse, currentUser.getUserId());
        }

        else if (note.isLocked()) {
            if (note.getPin() != null) {
                noteService.unlockNote(note.getId(), note.getPin(), currentUser.getUserId());
            }
        }
        //blur
        if (blurCheck.isSelected() != note.isBlurred()) {
            noteService.toggleBlur(note.getId(), currentUser.getUserId());
        }
        //Sec level
        if (!securityLevelCombo.getValue().equals(note.getSecurityLevel())) {
            note.setSecurityLevel(securityLevelCombo.getValue());
        }
    }

    @FXML
    private void onSetPin() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set PIN");
        dialog.setHeaderText("Set a PIN for this note");
        dialog.setContentText("Enter 4-8 digit PIN:");

        dialog.showAndWait().ifPresent(pin -> {
            if (pin.length() < 4 || pin.length() > 8) {
                AlertHelper.showError("PIN must be 4-8 digits");
                return;
            }

            if (!pin.matches("\\d+")) {
                AlertHelper.showError("PIN must contain only numbers");
                return;
            }

            // Confirm PIN
            TextInputDialog confirmDialog = new TextInputDialog();
            confirmDialog.setTitle("Confirm PIN");
            confirmDialog.setHeaderText("Confirm your PIN");
            confirmDialog.setContentText("Re-enter PIN:");
            confirmDialog.showAndWait().ifPresent(confirmPin -> {
                if (!pin.equals(confirmPin)) {
                    AlertHelper.showError("PINs do not match");
                    return;
                }

                try {
                    if (currentNote == null) {
                        // Store PIN temporarily for new note
                        currentPin = pin;
                        pinStatusLabel.setText("✅ PIN set (temporary)");
                        pinStatusLabel.setVisible(true);
                        AlertHelper.showSuccess("PIN set successfully!");
                    } else {
                        // Apply to existing note
                        noteService.lockNote(currentNote.getId(), pin, currentUser.getUserId());
                        pinStatusLabel.setText("✅ PIN set and note locked");
                        pinStatusLabel.setVisible(true);
                        lockCheck.setSelected(true);
                        setPinButton.setText("Change PIN");
                        AlertHelper.showSuccess("PIN set and note locked!");
                        loadNote(currentNote); // Refresh
                    }

                } catch (Exception e) {
                    AlertHelper.showError("Failed to set PIN: " + e.getMessage());
                }
            });
        });
    }

    @FXML
    private void onCancel() {closeEditor();}

    private void closeEditor() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
        else {
            SafeNotesApp.getInstance().showDashboard();
        }
    }
}
