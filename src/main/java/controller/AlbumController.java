package controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import app.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.User;
import util.SerializationUtil;

public class AlbumController {

    // Fields for album management (primary dashboard mode)
    @FXML
    private ListView<Album> albumListView; // Present in primary.fxml
    @FXML
    private TextField albumNameField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField tagTypeField;
    @FXML
    private TextField tagValueField;
    @FXML
    private TextField secondTagTypeField;
    @FXML
    private TextField secondTagValueField;

    // Fields for album details mode (photo listing)
    @FXML
    private Label albumNameLabel; // Present in album_details.fxml
    @FXML
    private ListView<Photo> photoListView; // Present in album_details.fxml

    private User currentUser;

    @FXML
    private void initialize() {
        // Determine mode by checking which key FXML fields are present.
        if (albumListView != null) {
            // Primary dashboard mode.
            currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                refreshAlbumList();
            }
        } else if (albumNameLabel != null) {
            // Album details mode.
            Album currentAlbum = SessionManager.getCurrentAlbum();
            if (currentAlbum != null) {
                albumNameLabel.setText(currentAlbum.getName());
                if (photoListView != null) {
                    refreshPhotoList();
                }
            } else {
                albumNameLabel.setText("No album selected");
            }
        }
    }

    // --- Album Management Methods ---
    @FXML
    private void handleCreateAlbum() {
        if (albumListView == null)
            return;
        String albumName = albumNameField.getText().trim();
        if (albumName.isEmpty()) {
            showError("Album name cannot be empty.");
            return;
        }
        for (Album album : currentUser.getAlbums()) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                showError("An album with this name already exists.");
                return;
            }
        }
        Album newAlbum = new Album(albumName);
        currentUser.addAlbum(newAlbum);
        refreshAlbumList();
        albumNameField.clear();
        saveUserData();
        showInfo("Album '" + albumName + "' created successfully.");
    }

    @FXML
    private void handleDeleteAlbum() {
        if (albumListView == null)
            return;
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to delete.");
            return;
        }
        currentUser.removeAlbum(selectedAlbum);
        refreshAlbumList();
        saveUserData();
        showInfo("Album '" + selectedAlbum.getName() + "' deleted successfully.");
    }

    @FXML
    private void handleRenameAlbum() {
        if (albumListView == null)
            return;
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to rename.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedAlbum.getName());
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Rename '" + selectedAlbum.getName() + "'");
        dialog.setContentText("Enter new album name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();
            for (Album album : currentUser.getAlbums()) {
                if (!album.equals(selectedAlbum) && album.getName().equalsIgnoreCase(newName)) {
                    showError("An album with this name already exists.");
                    return;
                }
            }
            selectedAlbum.renameAlbum(newName);
            refreshAlbumList();
            saveUserData();
            showInfo("Album renamed to '" + newName + "' successfully.");
        }
    }

    @FXML
    private void handleOpenAlbum() {
        if (albumListView != null) {
            // Primary mode: open selected album details.
            Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
            if (selectedAlbum == null) {
                showError("Please select an album to open.");
                return;
            }
            SessionManager.setCurrentAlbum(selectedAlbum);
            try {
                App.setRoot("album_details");
            } catch (IOException e) {
                showError("Failed to open album details view.");
                e.printStackTrace();
            }
        }
    }

    // --- Photo Operations (for album_details.fxml) ---
    @FXML
    private void handleAddPhoto() {
        Album currentAlbum = SessionManager.getCurrentAlbum();
        if (currentAlbum == null) {
            showError("No album selected.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            LocalDateTime dateTaken = LocalDateTime.now();
            Photo newPhoto = new Photo(selectedFile.getAbsolutePath(), "", dateTaken);
            currentAlbum.addPhoto(newPhoto);
            refreshPhotoList();
            saveUserData();
            showInfo("Photo added successfully.");
        }
    }

    @FXML
    private void handleDeletePhoto() {
        if (photoListView == null)
            return;
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        if (selectedPhoto == null) {
            showError("Please select a photo to delete.");
            return;
        }
        Album currentAlbum = SessionManager.getCurrentAlbum();
        if (currentAlbum != null) {
            currentAlbum.deletePhoto(selectedPhoto);
            refreshPhotoList();
            saveUserData();
            showInfo("Photo deleted successfully.");
        }
    }

    @FXML
    private void handleCopyPhoto() {
        if (photoListView == null)
            return;
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        if (selectedPhoto == null) {
            showError("Please select a photo to copy.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Copy Photo");
        dialog.setHeaderText("Enter destination album name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String destAlbumName = result.get().trim();
            User user = SessionManager.getCurrentUser();
            Album destinationAlbum = null;
            for (Album album : user.getAlbums()) {
                if (album.getName().equalsIgnoreCase(destAlbumName) && album != SessionManager.getCurrentAlbum()) {
                    destinationAlbum = album;
                    break;
                }
            }
            if (destinationAlbum == null) {
                showError("Destination album not found.");
                return;
            }
            destinationAlbum.addPhoto(selectedPhoto);
            saveUserData();
            showInfo("Photo copied to album '" + destAlbumName + "'.");
        }
    }

    @FXML
    private void handleMovePhoto() {
        if (photoListView == null)
            return;
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        if (selectedPhoto == null) {
            showError("Please select a photo to move.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Move Photo");
        dialog.setHeaderText("Enter destination album name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String destAlbumName = result.get().trim();
            User user = SessionManager.getCurrentUser();
            Album destinationAlbum = null;
            for (Album album : user.getAlbums()) {
                if (album.getName().equalsIgnoreCase(destAlbumName) && album != SessionManager.getCurrentAlbum()) {
                    destinationAlbum = album;
                    break;
                }
            }
            if (destinationAlbum == null) {
                showError("Destination album not found.");
                return;
            }
            Album currentAlbum = SessionManager.getCurrentAlbum();
            currentAlbum.deletePhoto(selectedPhoto);
            destinationAlbum.addPhoto(selectedPhoto);
            refreshPhotoList();
            saveUserData();
            showInfo("Photo moved to album '" + destAlbumName + "'.");
        }
    }

    @FXML
    private void handleOpenPhoto() {
        if (photoListView == null) {
            showError("Photo view not available.");
            return;
        }
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        if (selectedPhoto == null) {
            showError("Please select a photo to open.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photo_view.fxml"));
            Scene photoScene = new Scene(loader.load());
            PhotoController photoController = loader.getController();
            photoController.setCurrentAlbum(SessionManager.getCurrentAlbum());
            photoController.setSelectedPhoto(selectedPhoto);
            Stage stage = new Stage();
            stage.setTitle("Photo - " + selectedPhoto.getCaption());
            stage.setScene(photoScene);
            stage.show();
        } catch (IOException e) {
            showError("Failed to open photo view.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("primary");
        } catch (IOException e) {
            showError("Failed to return to primary view.");
            e.printStackTrace();
        }
    }

    // --- Utility Methods ---
    private void refreshAlbumList() {
        if (albumListView != null && currentUser != null) {
            albumListView.getItems().clear();
            albumListView.getItems().addAll(currentUser.getAlbums());
        }
    }

    private void refreshPhotoList() {
        if (photoListView != null && SessionManager.getCurrentAlbum() != null) {
            photoListView.getItems().clear();
            photoListView.getItems().addAll(SessionManager.getCurrentAlbum().getPhotos());
        }
    }

    private void saveUserData() {
        if (currentUser != null) {
            SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");
            //.
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Album Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Album Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
