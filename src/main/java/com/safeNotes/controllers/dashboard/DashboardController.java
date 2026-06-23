package com.safeNotes.controllers.dashboard;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.app.SafeNotesApp;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNotesListView();
        setupSearch();
        updateWelcome();
        loadNotes();
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
                    Label titleLabel = new Label(note.getTtitle());
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
        System.out.println("Opening note:" + note.getTtitle());
        //TO-DO: Check if note is blurred/lock
        welcomePane.setVisible(false);

        //TO-DO: Load note editor in content pane

        Label noteContent = new Label("Content of: " + note.getTtitle() + " ....... ");

        noteContent.setStyle("-fx-font-size: 14px; -fx-padding: 20px;");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(noteContent);
    }
    private void openNoteEditor(SecureNote note) {
        System.out.println("Opening note editor...");
        //TO-DO: load note editor from note_editor.fxml
        welcomeLabel.setVisible(false);

        //TO-DO: create the actual menu
        Label editorPlaceHolder = new Label("Note Editor PlaceHolder");

        editorPlaceHolder.setStyle("-fx-font-size: 14px; -fx-padding: 20px;");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(editorPlaceHolder);

    }
    private void filterNotes(String text) {
        // TO-DO: Implement filtering
        System.out.println("blah blah");
    }
    private void setupSearch() {
        searcField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterNotes(newVal);
        });
    }
    private void updateWelcome() {
        //TO-DO: load actual username
        welcomeLabel.setText("Welcome ");
    }
    private void loadNotes() {
        //TO-DO: load actual notes

        updateStatus("Loaded " + notesListView.getItems().size() + " notes");
    }
    private void updateStatus(String message) {
        statusLabel.setText(message);
        System.out.println("Status: " + message);
    }

    @FXML
    private void onLockApp() {
        System.out.println("Locking app...");
        //TO-DO: Clear sensitive data and return to login
        goToLogin();
    }
    @FXML
    private void onLogout() {
        System.out.println("Logging out...");
        //TO-DO: Clear session data
        goToLogin();
    }
    @FXML
    private void onCreateNewNote() {
        System.out.println("Creating new Note...");
        //TO-DO: Open note editor
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
        System.out.println("Opening Account Settings...");
        //TO-DO: Open actual Account Settings list
    }
    @FXML
    private void onSecuritySettings() {
        System.out.println("Opening Security Settings...");
        //TO-DO: Open actual Security Settings list
    } 

    private void goToLogin() {
        try {
            SafeNotesApp app = new SafeNotesApp();
            app.showLoginScreen();
        } 
        catch (NullPointerException e) {
            System.err.println("Resource not found(CSS, icon)");
        }
        catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
        }
    }
}
