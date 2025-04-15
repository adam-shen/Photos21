package controller;

import java.io.IOException;
import java.util.Optional;

import app.Photos;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Album;
import model.User;
import util.SerializationUtil;

public class UserController {

    @FXML
    private TableView<Album> albumTableView;
    @FXML
    private TableColumn<Album, String> albumNameColumn;
    @FXML
    private TableColumn<Album, Integer> photoCountColumn;
    @FXML
    private TableColumn<Album, String> dateRangeColumn;

    @FXML
    private void initialize() {
        User currentUser = getCurrentUser();
        System.out.println("Initializing user view for: " + currentUser.getUsername());

        // Configure the TableColumn cell value factories
        albumNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        photoCountColumn.setCellValueFactory(new PropertyValueFactory<>("photoCount"));
        dateRangeColumn.setCellValueFactory(new PropertyValueFactory<>("dateRange"));

        // Load the user's albums into the TableView
        if (albumTableView != null && currentUser.getAlbums() != null) {
            albumTableView.getItems().addAll(currentUser.getAlbums());
        }
    }

    @FXML
    private void handleLogout() {
        saveUserData();
        try {
            Photos.setRoot("login"); // Redirect to the login screen
        } catch (IOException e) {
            showError("Failed to load the login screen.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuit() {
        // Save any pending changes before exiting
        saveUserData();
        Platform.exit();
    }

    @FXML
    private void handleOpenAlbum() {
        Album selectedAlbum = albumTableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to open.");
            return;
        }

        // Set the current album for later retrieval
        SessionManager.setCurrentAlbum(selectedAlbum);

        try {
            // Switch to the album details view (album_details.fxml)
            Photos.setRoot("album_details");
        } catch (IOException e) {
            showError("Failed to load the album details view.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddAlbum() {
        // Create a dialog to prompt for the new album's name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Album");
        dialog.setHeaderText("Create a new album");
        dialog.setContentText("Enter album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String albumName = result.get().trim();

            // Retrieve the current user (ensure this method works for your project)
            User currentUser = getCurrentUser(); // Alternatively, use SessionManager.getCurrentUser()

            // Check if an album with the same name already exists
            for (Album album : currentUser.getAlbums()) {
                if (album.getName().equalsIgnoreCase(albumName)) {
                    showError("An album with this name already exists.");
                    return;
                }
            }

            // No duplicate found; create the album
            Album newAlbum = new Album(albumName);
            currentUser.addAlbum(newAlbum);

            // If you're using a TableView (or ListView) to display albums, update it
            if (albumTableView != null) {
                albumTableView.getItems().add(newAlbum);
            }

            // Save the updated user data to disk
            SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");

            showInfo("Album '" + albumName + "' created successfully.");
        }
    }

    @FXML
    private void handleDeleteAlbum() {
        Album selectedAlbum = albumTableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to delete.");
            return;
        }

        User currentUser = getCurrentUser();
        currentUser.removeAlbum(selectedAlbum);

        // Update the TableView
        albumTableView.getItems().remove(selectedAlbum);

        showInfo("Album '" + selectedAlbum.getName() + "' deleted successfully.");
    }

    @FXML
    private void handleRenameAlbum() {
        Album selectedAlbum = albumTableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to rename.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedAlbum.getName());
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Rename the selected album");
        dialog.setContentText("Enter new album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();
            selectedAlbum.renameAlbum(newName);

            // Update the TableView
            albumTableView.refresh();

            showInfo("Album renamed to '" + newName + "' successfully.");
        }
    }

    // Utility methods for UserController
    private User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    private void saveUserData() {
        // Logic to save user data to disk
        // This would use SerializationUtil in a real implementation
        SerializationUtil.save(SessionManager.getCurrentUser(),
                "data/users/" + SessionManager.getCurrentUser().getUsername() + ".dat");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openSearchView() {
        try {
            Photos.setRoot("search_view");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}