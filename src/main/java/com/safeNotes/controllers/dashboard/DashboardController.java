package com.safeNotes.controllers.dashboard;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.models.domain.User;
import com.safeNotes.repositories.SQLNoteRepository;
import com.safeNotes.services.auth.SessionManager;
import com.safeNotes.services.encryption.Argon2Hasher;
import com.safeNotes.services.notes.NoteService;
import com.safeNotes.services.notes.NoteServiceImpl;
import com.safeNotes.app.SafeNotesApp;
import com.safeNotes.controllers.notes.NoteEditorController;
import com.safeNotes.utils.gui.AlertHelper;
import com.safeNotes.utils.gui.ViewLoader;
import javafx.scene.Parent;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label securityStatusLabel;
    @FXML private Label lastSyncLabel;
    @FXML private TextField searcField;
    @FXML private ListView<SecureNote> notesListView;
    @FXML private StackPane contentPane;
    @FXML private VBox welcomePane;
    private ToggleGroup filterGroup = new ToggleGroup();
    private NoteService noteService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
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
            if (newNote != null) {
                openNote(newNote);
            }
        });
    }

    private void openNote(SecureNote note) {
        System.out.println("Opening note:" + note.getTitle());
        
        if (note.isLocked()) {
            promptForPinAndOpen(note);
            return;
        }

        welcomePane.setVisible(false);
        Label noteContent = new Label("Content of: " + note.getTitle() + "\n\n" + note.getContent());

        noteContent.setStyle("-fx-font-size: 14px; -fx-padding: 20px;");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(noteContent);
    }
    private void openNoteEditor(SecureNote note) {
        try {
            welcomePane.setVisible(false);

            //Load note editor
            Parent editorRoot = ViewLoader.loadParent("/com/safeNotes/views/fxml/notes/note_editor.fxml");

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
        searcField.textProperty().addListener((obs, oldVal, newVal) -> {
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
        AlertHelper.showInfo("Security settings will be available later");
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
        TextInputDialog pinDialog = new TextInputDialog();
        pinDialog.setTitle("Note Locked");
        pinDialog.setHeaderText("This note is locked");
        pinDialog.setContentText("Enter PIN to unlock:");

        pinDialog.showAndWait().ifPresent(pin -> {
            try {
                if (noteService.verifyPin(note.getId(), pin, currentUser.getUserId())) {
                    noteService.unlockNote(note.getId(), pin, currentUser.getUserId());
                    loadNotes();
                    SecureNote updated = noteService.getNoteById(note.getId(), currentUser.getUserId());
                    openNote(updated);
                }
                else {
                    AlertHelper.showError("Incorrect Pin");
                }
            }
            catch (Exception e) {
                AlertHelper.showError("Failed to unlock: " + e.getMessage());
            }
        });
    }

    private void showWelcomePane() {
        welcomePane.setVisible(true);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(welcomePane);
    }
}
