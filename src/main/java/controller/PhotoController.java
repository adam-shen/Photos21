package controller;

import java.io.File;
import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Album;
import model.Photo;
import model.Tag;
import model.User;

public class PhotoController {

    @FXML
    private ImageView photoImageView;

    @FXML
    private TextField photoCaptionField;

    @FXML
    private ListView<String> tagListView;

    @FXML
    private ComboBox<String> albumComboBox;

    @FXML
    private TextField tagNameField;

    @FXML
    private TextField tagValueField;

    private Album currentAlbum;
    private Photo selectedPhoto;
    private User currentUser;

    @FXML
    private void initialize() {
        // This would be called after the album is opened
        // and currentAlbum is set
        currentUser = SessionManager.getCurrentUser();
    }

    public void setCurrentAlbum(Album album) {
        this.currentAlbum = album;

        // Populate the album combo box with all albums from the user
        albumComboBox.getItems().clear();
        for (Album userAlbum : currentUser.getAlbums()) {
            albumComboBox.getItems().add(userAlbum.getName());
        }
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Create new photo with file's last modified date as the date taken
            LocalDateTime dateTaken = LocalDateTime.now(); // Ideally use file's last modified date
            Photo newPhoto = new Photo(selectedFile.getAbsolutePath(), "", dateTaken);

            // Add the photo to the current album
            currentAlbum.addPhoto(newPhoto);

            // Update the UI
            showInfo("Photo added successfully.");

            // Select the newly added photo
            setSelectedPhoto(newPhoto);
        }
    }

    @FXML
    private void handleDeletePhoto() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        currentAlbum.deletePhoto(selectedPhoto);

        // Clear the display
        photoImageView.setImage(null);
        photoCaptionField.clear();
        tagListView.getItems().clear();

        showInfo("Photo deleted successfully.");
    }

    @FXML
    private void handleEditCaption() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        String newCaption = photoCaptionField.getText().trim();
        selectedPhoto.setCaption(newCaption);

        showInfo("Caption updated successfully.");
    }

    @FXML
    private void handleAddTag() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        String tagName = tagNameField.getText().trim();
        String tagValue = tagValueField.getText().trim();

        if (tagName.isEmpty() || tagValue.isEmpty()) {
            showError("Tag name and value cannot be empty.");
            return;
        }

        // Create and add the tag
        Tag newTag = new Tag(tagName, tagValue);
        selectedPhoto.addTag(newTag);

        // Update tag list
        refreshTagList();

        // Clear input fields
        tagNameField.clear();
        tagValueField.clear();

        showInfo("Tag added successfully.");
    }

    @FXML
    private void handleDeleteTag() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        String selectedTag = tagListView.getSelectionModel().getSelectedItem();
        if (selectedTag == null) {
            showError("No tag selected.");
            return;
        }

        // Parse the tag from the format "name=value"
        String[] parts = selectedTag.split("=");
        if (parts.length == 2) {
            Tag tagToRemove = new Tag(parts[0], parts[1]);
            selectedPhoto.removeTag(tagToRemove);

            // Update tag list
            refreshTagList();

            showInfo("Tag removed successfully.");
        }
    }

    @FXML
    private void handleCopyPhoto() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        String selectedAlbumName = albumComboBox.getValue();
        if (selectedAlbumName == null) {
            showError("Please select a destination album.");
            return;
        }

        // Find the destination album
        Album destinationAlbum = null;
        for (Album album : currentUser.getAlbums()) {
            if (album.getName().equals(selectedAlbumName)) {
                destinationAlbum = album;
                break;
            }
        }

        if (destinationAlbum == null) {
            showError("Destination album not found.");
            return;
        }

        // Add the photo to the destination album
        destinationAlbum.addPhoto(selectedPhoto);

        showInfo("Photo copied to album '" + selectedAlbumName + "' successfully.");
    }

    @FXML
    private void handleMovePhoto() {
        if (selectedPhoto == null) {
            showError("No photo selected.");
            return;
        }

        String selectedAlbumName = albumComboBox.getValue();
        if (selectedAlbumName == null) {
            showError("Please select a destination album.");
            return;
        }

        // Find the destination album
        Album destinationAlbum = null;
        for (Album album : currentUser.getAlbums()) {
            if (album.getName().equals(selectedAlbumName)) {
                destinationAlbum = album;
                break;
            }
        }

        if (destinationAlbum == null) {
            showError("Destination album not found.");
            return;
        }

        // Remove from current album and add to destination
        currentAlbum.deletePhoto(selectedPhoto);
        destinationAlbum.addPhoto(selectedPhoto);

        // Clear the display since the photo is no longer in the current album
        photoImageView.setImage(null);
        photoCaptionField.clear();
        tagListView.getItems().clear();

        showInfo("Photo moved to album '" + selectedAlbumName + "' successfully.");
    }

    @FXML
    private void handleNextPhoto() {
        if (currentAlbum == null || currentAlbum.getPhotos().isEmpty()) {
            return;
        }

        int currentIndex = currentAlbum.getPhotos().indexOf(selectedPhoto);
        int nextIndex = (currentIndex + 1) % currentAlbum.getPhotos().size();

        setSelectedPhoto(currentAlbum.getPhotos().get(nextIndex));
    }

    @FXML
    private void handlePreviousPhoto() {
        if (currentAlbum == null || currentAlbum.getPhotos().isEmpty()) {
            return;
        }

        int currentIndex = currentAlbum.getPhotos().indexOf(selectedPhoto);
        int previousIndex = (currentIndex - 1 + currentAlbum.getPhotos().size()) % currentAlbum.getPhotos().size();

        setSelectedPhoto(currentAlbum.getPhotos().get(previousIndex));
    }

    public void setSelectedPhoto(Photo photo) {
        this.selectedPhoto = photo;

        if (photo != null) {
            // Load the image
            try {
                Image image = new Image("file:" + photo.getFilepath());
                photoImageView.setImage(image);
            } catch (Exception e) {
                photoImageView.setImage(null);
                showError("Failed to load the image.");
            }

            // Update caption field
            photoCaptionField.setText(photo.getCaption());

            // Update tag list
            refreshTagList();
        }
    }

    private void refreshTagList() {
        tagListView.getItems().clear();

        if (selectedPhoto != null) {
            for (Tag tag : selectedPhoto.getTags()) {
                tagListView.getItems().add(tag.getName() + "=" + tag.getValue());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Photo Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Photo Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}