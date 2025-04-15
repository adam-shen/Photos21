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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
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

    // Fields for album details mode (photo grid display)
    @FXML
    private Label albumNameLabel; // Present in album_details.fxml
    @FXML
    private TilePane photoTilePane; // New: used for grid display of photos

    private User currentUser;
    // We'll track the selected photo.
    private Photo selectedPhoto;
    // This will reference the StackPane wrapping the current selected thumbnail.
    private StackPane selectedThumbnailContainer;

    @FXML
    private void initialize() {
        // Determine mode by checking which FXML components are present.
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
                if (photoTilePane != null) {
                    refreshPhotoGrid();
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

    // --- Photo Operations (for album_details.fxml using the photoTilePane) ---
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
            // Check if the album already contains a photo with the same file path.
            boolean duplicateFound = currentAlbum.getPhotos().stream()
                    .anyMatch(photo -> photo.getFilepath().equalsIgnoreCase(selectedFile.getAbsolutePath()));
            if (duplicateFound) {
                showError("The selected photo already exists in this album.");
                return;
            }
            LocalDateTime dateTaken = LocalDateTime.now();
            Photo newPhoto = new Photo(selectedFile.getAbsolutePath(), "", dateTaken);
            currentAlbum.addPhoto(newPhoto);
            refreshPhotoGrid();
            saveUserData();
            showInfo("Photo added successfully.");
        }
    }

    @FXML
    private void handleDeletePhoto() {
        if (selectedPhoto == null) {
            showError("Please select a photo to delete.");
            return;
        }
        Album currentAlbum = SessionManager.getCurrentAlbum();
        if (currentAlbum != null) {
            currentAlbum.deletePhoto(selectedPhoto);
            selectedPhoto = null; // reset selection
            selectedThumbnailContainer = null;
            refreshPhotoGrid();
            saveUserData();
            showInfo("Photo deleted successfully.");
        }
    }

    @FXML
    private void handleCopyPhoto() {
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
            selectedPhoto = null;
            selectedThumbnailContainer = null;
            refreshPhotoGrid();
            saveUserData();
            showInfo("Photo moved to album '" + destAlbumName + "'.");
        }
    }

    @FXML
    private void handleOpenPhoto() {
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

    // Populates the photoTilePane as a grid of thumbnails.
    private void refreshPhotoGrid() {
        if (photoTilePane != null && SessionManager.getCurrentAlbum() != null) {
            photoTilePane.getChildren().clear();
            for (Photo photo : SessionManager.getCurrentAlbum().getPhotos()) {
                // Use the new createThumbnail method.
                StackPane thumbnailContainer = createThumbnail(photo);
                photoTilePane.getChildren().add(thumbnailContainer);
            }
        }
    }

    // Helper method to create a thumbnail container (StackPane wrapping an
    // ImageView)
    // that supports selection highlighting.
    private StackPane createThumbnail(Photo photo) {
        double thumbnailWidth = 150;
        double thumbnailHeight = 150;
        Image image = new Image("file:" + photo.getFilepath(), thumbnailWidth, thumbnailHeight, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(thumbnailWidth);
        imageView.setFitHeight(thumbnailHeight);

        // Wrap the ImageView in a StackPane.
        StackPane container = new StackPane(imageView);
        // Default style: no border.
        container.setStyle("-fx-border-color: transparent; -fx-padding: 2;");

        // Handle mouse clicks on the thumbnail.
        container.setOnMouseClicked(e -> {
            // Clear any previous selection highlight.
            if (selectedThumbnailContainer != null) {
                selectedThumbnailContainer.setStyle("-fx-border-color: transparent; -fx-padding: 2;");
            }
            // Set the new selection.
            selectedPhoto = photo;
            selectedThumbnailContainer = container;
            // Apply highlight style.
            container.setStyle("-fx-border-color: blue; -fx-border-width: 3px; -fx-padding: 2;");
            // If double-clicked, open the photo.
            if (e.getClickCount() == 2) {
                handleOpenPhoto();
            }
        });
        return container;
    }

    private void saveUserData() {
        if (currentUser != null) {
            SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");
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
