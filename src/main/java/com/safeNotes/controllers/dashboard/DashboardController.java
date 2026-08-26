package com.safeNotes.controllers.dashboard;

import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.models.domain.User;
import com.safeNotes.repositories.SQLNoteRepository;
import com.safeNotes.repositories.SQLUserRepository;
import com.safeNotes.services.auth.AuthenticationService;
import com.safeNotes.services.auth.AuthenticationServiceImpl;
import com.safeNotes.services.auth.SessionManager;
import com.safeNotes.services.encryption.Argon2Hasher;
import com.safeNotes.services.notes.NoteService;
import com.safeNotes.services.notes.NoteServiceImpl;
import com.safeNotes.app.SafeNotesApp;
import com.safeNotes.controllers.notes.NoteEditorController;
import com.safeNotes.utils.gui.AlertHelper;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label securityStatusLabel;
    @FXML private Label lastSyncLabel;
    @FXML private TextField searchField;
    @FXML private ListView<SecureNote> notesListView;
    @FXML private StackPane contentPane;
    @FXML private VBox welcomePane;
    private NoteService noteService;
    private User currentUser;
    private static DashboardController instance;
    @FXML private Label totalNotesLabel;
    @FXML private Label lockedNotesLabel;
    @FXML private Label blurredNotesLabel;
    private AuthenticationService authService;
    @FXML private ListView<String> trustedLocationsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            authService = new AuthenticationServiceImpl(
                new SQLUserRepository(), 
                new Argon2Hasher(),
                SessionManager.getInstance()
            );

            noteService = new NoteServiceImpl(new SQLNoteRepository(), new Argon2Hasher());

            currentUser = SessionManager.getInstance().getCurrentUser();
            setupNotesListView();
            setupSearch();
            updateWelcome();
            loadNotes();
        }
        catch (Exception e) {
            AlertHelper.showError("Failed to initialize: " + e.getMessage());
        }
    }
    
    private void setupNotesListView() {
        notesListView.setCellFactory(p -> new ListCell<>() {
            protected void updateItem(SecureNote note, boolean empty) {
                super.updateItem(note, empty);

                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    HBox cellContent = new HBox(10);
                    cellContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Label icon = new Label();

                    if (note.isLocked()) {
                        icon.setText("🔒");
                        icon.getStyleClass().add("locked-icon");
                    }
                    else if (note.isBlurred()) {
                        icon.setText("👁️");
                        icon.getStyleClass().add("blurred-icon");
                    }
                    else {
                        icon.setText("📝");
                        icon.getStyleClass().add("normal-icon");
                    }

                    VBox noteInfo = new VBox(2);
                    Label titleLabel = new Label(note.getTitle());
                    titleLabel.getStyleClass().add("note-title");

                    Label dateLabel = new Label(note.getFormattedDate());
                    dateLabel.getStyleClass().add("note-date");

                    noteInfo.getChildren().addAll(titleLabel, dateLabel);
                    cellContent.getChildren().addAll(icon, noteInfo);
                    setGraphic(cellContent);
                }
            }
        });

        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if (newNote != null && newNote != oldNote) {
                previewNote(newNote);;
            }
        });
    }

    private void openNote(SecureNote note) {
        System.out.println("Opening note:" + note.getTitle());
        
        if (note.isLocked()) {
            promptForPinAndOpen(note);
            return;
        }

        openNoteEditor(note);
    }
    private void openNoteEditor(SecureNote note) {
        try {
            welcomePane.setVisible(false);

            //Get controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/safeNotes/views/fxml/notes/note_editor.fxml"));
            Parent root = loader.load();
            NoteEditorController controller = loader.getController();
            controller.loadNote(note);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);
        }
        catch (Exception e) {
            AlertHelper.showError("Failed to open editor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void filterNotes(String text) {
        if (text == null || text.trim().isEmpty()) {
            loadNotes();
            return;
        }
        String lowerSearch = text.toLowerCase().trim();
        List<SecureNote> filtered = notesListView.getItems().stream().filter(note -> note.getTitle().toLowerCase().contains(lowerSearch)).collect(Collectors.toList());
        notesListView.getItems().setAll(filtered);
        updateStatus("Found " + filtered.size() + " notes");
    }
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterNotes(newVal);
        });
    }
    private void updateWelcome() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
        else {
            welcomeLabel.setText("Welcome, User");
        }
    }
    private void loadNotes() {
        if (currentUser == null) {
            updateStatus("No user logged in");
            return;
        }
        try {
            List<SecureNote> notes = noteService.getNotesByUser(currentUser.getUserId());
            notesListView.getItems().setAll(notes);
            updateStats(notes);
            updateStatus("Loaded " + notes.size() + " notes");
        }
        catch (Exception e) {
            updateStatus("Error loading notes: " + e.getMessage());
        }
    }
    private void updateStatus(String message) {
        statusLabel.setText(message);
        System.out.println("Status: " + message);
    }

    @FXML
    private void onLockApp() {
        SessionManager.getInstance().clearSession();
        goToLogin();
    }
    @FXML
    private void onLogout() {
        SessionManager.getInstance().clearSession();
        goToLogin();
    }
    @FXML
    private void onCreateNewNote() {
        System.out.println("Creating new Note...");
        openNoteEditor(null); // new note
    }
    @FXML
    private void onRefresh() {
        System.out.println("Refreshing notes...");
        notesListView.getItems().clear();
        loadNotes();
    }
    @FXML
    private void onAccountSettings() {
        AlertHelper.showInfo("Account settings will be available later");
    }
    @FXML
    private void onSecuritySettings() {
        try {
            Map<String, String> locations = authService.getTrustedLocationMap(currentUser.getUsername());
            StringBuilder sb = new StringBuilder("Trusted locations:\n");
            if (locations.isEmpty()) {
                sb.append("No trusted locations added yet.");
            }
            else {
                for (int i = 0; i < locations.size(); i++) {
                    sb.append((i + 1)).append(". ").append(locations.get(i)).append("\n");
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Trusted Locations");
            alert.setHeaderText("Your trusted locations");
            alert.setContentText(sb.toString());
            alert.showAndWait();
        }
        catch (Exception e) {
            AlertHelper.showError("Failed to load locations " + e.getMessage());
        }
    } 

    private void goToLogin() {
        try {
            SafeNotesApp.getInstance().showLoginScreen();
        } 
        catch (NullPointerException e) {
            System.err.println("Resource not found(CSS, icon)");
        }
        catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
        }
    }

    private void promptForPinAndOpen(SecureNote note) {
        Dialog<String> pinDialog = new Dialog<>();
        pinDialog.initOwner(contentPane.getScene().getWindow());
        pinDialog.initModality(Modality.APPLICATION_MODAL);
        pinDialog.setTitle("Note Locked");
        pinDialog.setHeaderText("This note is locked");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label label = new Label("Enter Pin to unlock:");
        PasswordField pinField = new PasswordField();  
        pinField.setPromptText("Enter Pin");
        
        content.getChildren().addAll(label, pinField);
        pinDialog.getDialogPane().setContent(content);
        
        ButtonType unlockButton = new ButtonType("Unlock", ButtonBar.ButtonData.OK_DONE);
        ButtonType forgotButton = new ButtonType("Forgot PIN?", ButtonBar.ButtonData.HELP_2);
        pinDialog.getDialogPane().getButtonTypes().setAll(unlockButton, forgotButton);
        
        Button forgotBtn = (Button) pinDialog.getDialogPane().lookupButton(forgotButton);
        forgotBtn.setOnAction(e -> {
            pinDialog.close();
            handleForgotPin(note);
        });
        
        pinDialog.setResultConverter(dialogButton -> {
            if (dialogButton == unlockButton) {
                return pinField.getText();
            }
            return null;
        });
        
        pinDialog.showAndWait().ifPresent(pin -> {
            if (pin != null && !pin.trim().isEmpty()) {
                try {
                    if (noteService.verifyPin(note.getId(), pin, currentUser.getUserId())) {
                        noteService.unlockNote(note.getId(), pin, currentUser.getUserId());
                        loadNotes();
                        notesListView.getSelectionModel().clearSelection();
                        SecureNote updated = noteService.getNoteById(note.getId(), currentUser.getUserId());
                        openNoteEditor(updated);
                    } else {
                        AlertHelper.showError("Incorrect PIN");
                    }
                } catch (Exception e) {
                    AlertHelper.showError("Failed to unlock: " + e.getMessage());
                }
            }
        });
    } 

    private void showWelcomePane() {
        welcomePane.setVisible(true);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(welcomePane);
    }

    private void previewNote(SecureNote note) {
        welcomePane.setVisible(false);

        VBox previewBox = new VBox(15);
        previewBox.setPadding(new Insets(30));
        previewBox.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Label titleLabel = new Label(note.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");        

        //Metadata
        Label metaLabel = new Label("Created: " + note.getFormattedDate());
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        //Content preview
        TextArea contentArea = new TextArea(note.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setStyle("-fx-font-size: 14px;");
        contentArea.setPrefHeight(400);

        //Security indicators
        HBox securityIcons = new HBox(10);
        if (note.isLocked()) {
            securityIcons.getChildren().add(new Label("🔒 Locked"));
            contentArea.setText("🔒 This note is locked. Enter PIN to view.");
            contentArea.setEditable(false);
        }
        else {
            contentArea.setText(note.getContent());
            contentArea.setEditable(false);
        }
        if (note.isBlurred()) {
            securityIcons.getChildren().add(new Label("👁️ Blurred"));
        }
        
        //Buttons
        HBox buttonBox = new HBox(10);
        Button openButton = new Button("Open Note");
        openButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        openButton.setOnAction(e -> openNote(note));     
        
        Button deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setVisible(!note.isLocked());
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Note");
            confirm.setHeaderText("Delete: " + note.getTitle());
            confirm.setContentText("Are you sure?");
            confirm.initOwner(contentPane.getScene().getWindow());
            confirm.initModality(Modality.APPLICATION_MODAL);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        noteService.deleteNote(note.getId(), currentUser.getUserId());
                        loadNotes();
                        showWelcomePane();
                        AlertHelper.showSuccess("Note deleted.");
                    }
                    catch (Exception ex) {
                        AlertHelper.showError("Failed to delete: " + ex.getMessage());
                    }
                }
            });

        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(openButton, spacer, deleteButton);
        previewBox.getChildren().addAll(titleLabel, metaLabel, securityIcons, contentArea, buttonBox);

        contentPane.getChildren().clear();
        contentPane.getChildren().add(previewBox);
    }

    public static void refreshNotes() {
        if (instance != null) {Platform.runLater(() -> instance.loadNotes());}
    }

    private void updateStats(List<SecureNote> notes) {
        int total = notes.size();
        int locked = (int) notes.stream().filter(SecureNote::isLocked).count();
        int blurred = (int) notes.stream().filter(SecureNote::isBlurred).count();

        totalNotesLabel.setText(String.valueOf(total));
        lockedNotesLabel.setText(String.valueOf(locked));
        blurredNotesLabel.setText(String.valueOf(blurred));
    }

    private void handleForgotPin(SecureNote note) {
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.initOwner(contentPane.getScene().getWindow());
        passwordDialog.initModality(Modality.APPLICATION_MODAL);
        passwordDialog.setTitle("Verify Identity");
        passwordDialog.setHeaderText("Enter your master password to reset Pin");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label label = new Label("Master Password:");
        PasswordField passwordField = new PasswordField();  
        passwordField.setPromptText("Enter master password");
        
        content.getChildren().addAll(label, passwordField);
        passwordDialog.getDialogPane().setContent(content);
        
        ButtonType verifyButton = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(verifyButton, cancelButton);
        
        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == verifyButton) {
                return passwordField.getText();
            }
            return null;
        });
        
        passwordDialog.showAndWait().ifPresent(masterPassword -> {
            if (masterPassword != null && !masterPassword.trim().isEmpty()) {
                try {
                    if (authService.verifyMasterPassword(currentUser.getUsername(), masterPassword)) {
                        noteService.removePin(note.getId(), currentUser.getUserId());
                        AlertHelper.showSuccess("Pin removed. Note is now unlocked.");
                        loadNotes();
                        openNoteEditor(note);
                    } else {
                        AlertHelper.showError("Incorrect master password");
                    }
                } catch (Exception e) {
                    AlertHelper.showError("Failed to reset Pin: " + e.getMessage());
                }
            }
        });
    } 


    /*@FXML
    private void onAddLocation() {
        String ip = locationInput.getText().trim();
        if (ip.isEmpty()) {
            AlertHelper.showError("Please enter an IP address");
            return;
        }
        try {
            String locationHash = hashLocation(ip);
            authService.addTrustedLocation(currentUser.getUserId(), locationHash);
            AlertHelper.showSuccess("Location added to trusted list");
            refreshTrustedLocationsList();
        }
        catch (Exception e) {
            AlertHelper.showError("Failed to add location: " + e.getMessage());
        }
    }*/
  
    /*@FXML
    private void onRemoveLocation() {
        String selected = trustedLocationsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Please select a location to remove");
            return;
        }
        try {
            authService.removeTrustedLocation(currentUser.getUserId(), selected);
            AlertHelper.showSuccess("Location removed");
            refreshTrustedLocationsList();
        } catch (Exception e) {
            AlertHelper.showError("Failed to remove location: " + e.getMessage());
        }
    }*/

    private String hashLocation(String location) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(location.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return location;
        }
    }
    @FXML
    private void onManageLocations() {
        Stage locationStage = new Stage();
        locationStage.initOwner(contentPane.getScene().getWindow());
        locationStage.initModality(Modality.APPLICATION_MODAL);
        locationStage.setTitle("Manage Trusted Locations");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(380);
        
        // Current location
        HBox currentBox = new HBox(10);
        currentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label currentLabel = new Label("Current location: " + getCurrentLocation());
        CheckBox trustCurrentCheck = new CheckBox("Trust this location");
        
        ListView<String> locationsListView = new ListView<>();
        locationsListView.setPrefHeight(150);
        refreshTrustedLocationsList(locationsListView);
        
        trustCurrentCheck.setOnAction(e -> {
            if (trustCurrentCheck.isSelected()) {
                try {
                    String currentIP = getCurrentLocation();
                    String hash = hashLocation(currentIP);
                    
                    List<String> existingHashes = authService.getTrustedLocationHashes(currentUser.getUsername());
                    if (existingHashes.contains(hash)) {
                        AlertHelper.showWarning("Current location is already in your trusted locations");
                        trustCurrentCheck.setSelected(false);
                        return;
                    }
                    
                    authService.addTrustedLocation(currentUser.getUsername(), currentIP);
                    AlertHelper.showSuccess("Current location added to trusted");
                    refreshTrustedLocationsList(locationsListView);
                    trustCurrentCheck.setSelected(false);
                } catch (Exception ex) {
                    AlertHelper.showError("Failed to add: " + ex.getMessage());
                    trustCurrentCheck.setSelected(false);
                }
            }
        });
        
        currentBox.getChildren().addAll(currentLabel, trustCurrentCheck);
        
        // Add location
        HBox addBox = new HBox(10);
        TextField locationInput = new TextField();
        locationInput.setPromptText("Enter IP address");
        Button addButton = new Button("Add Location");

        addButton.setOnAction(e -> {
            String ip = locationInput.getText().trim();
            if (ip.isEmpty()) {
                AlertHelper.showError("Please enter an IP address");
                return;
            }
            
            // Validate IP format
            if (!isValidIP(ip)) {
                AlertHelper.showError("Invalid IP address format");
                return;
            }
            
            try {
                String hash = hashLocation(ip);
                List<String> existingHashes = authService.getTrustedLocationHashes(currentUser.getUsername());
                if (existingHashes.contains(hash)) {
                    AlertHelper.showWarning("This IP is already in your trusted locations");
                    return;
                }
                
                authService.addTrustedLocation(currentUser.getUsername(), hash);
                AlertHelper.showSuccess("Location added");
                locationInput.clear();
                refreshTrustedLocationsList(locationsListView);
            } catch (Exception ex) {
                AlertHelper.showError("Failed to add: " + ex.getMessage());
            }
        });

        addBox.getChildren().addAll(locationInput, addButton);
        
        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            String selectedHash = locationsListView.getSelectionModel().getSelectedItem();
            if (selectedHash == null) {
                AlertHelper.showError("Please select a location to remove");
                return;
            }
            try {
                // Find the IP for this hash
                Map<String, String> map = authService.getTrustedLocationMap(currentUser.getUsername());
                String ipToRemove = null;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getValue().equals(selectedHash)) {
                        ipToRemove = entry.getKey();
                        break;
                    }
                }
                if (ipToRemove != null) {
                    authService.removeTrustedLocation(currentUser.getUsername(), ipToRemove);
                    AlertHelper.showSuccess("Location removed");
                    refreshTrustedLocationsList(locationsListView);
                } else {
                    AlertHelper.showError("Could not find location to remove");
                }
            } catch (Exception ex) {
                AlertHelper.showError("Failed to remove: " + ex.getMessage());
            }
        });
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> locationStage.close());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(removeButton, closeButton);
        
        content.getChildren().addAll(currentBox, addBox, locationsListView, buttonBox);
        
        Scene scene = new Scene(content);
        locationStage.setScene(scene);
        locationStage.showAndWait();
    }

    private void refreshTrustedLocationsList(ListView<String> listView) {
        if (currentUser == null) return;
        try {
            Map<String, String> map = authService.getTrustedLocationMap(currentUser.getUsername());
            listView.getItems().clear();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    listView.getItems().add(entry.getValue());  // Show hash
                }
            }
        } catch (Exception e) {
            AlertHelper.showError("Failed to refresh: " + e.getMessage());
        }
    }

    private String getCurrentLocation() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isBlank()) return false;
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }
}
