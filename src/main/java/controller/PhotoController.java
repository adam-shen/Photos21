package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Album;
import model.Photo;
import model.Tag;
import model.User;
import util.SerializationUtil;

public class PhotoController {

    @FXML
    private ImageView photoImageView;

    @FXML
    private TextField photoCaptionField; // Editable caption field

    @FXML
    private Label photoDateLabel; // New: displays date taken

    @FXML
    private ListView<String> tagListView;

    @FXML
    private ComboBox<String> tagTypeComboBox; // For tag type selection

    @FXML
    private TextField tagValueField; // For tag value entry

    @FXML
    private ComboBox<String> albumComboBox;

    // (Optional: If photoListView is not used in this view, you can leave it.)
    @FXML
    private ListView<Photo> photoListView;

    private Album currentAlbum;
    private Photo selectedPhoto;
    private User currentUser;

    // A static set of known tag types to persist for the session.
    private static final Set<String> knownTagTypes = new HashSet<>();

    @FXML
    private void initialize(URL url, ResourceBundle rb) {
        // Initialize currentUser from the SessionManager
        currentUser = SessionManager.getCurrentUser();

        // Add default tag types if not already present.
        if (knownTagTypes.isEmpty()) {
            knownTagTypes.add("location"); // This tag will be single-value (enforced in Photo.addTag)
            knownTagTypes.add("person"); // Multiple values allowed
        }

        // Populate the tag type combo box
        refreshTagTypeComboBox();
    }

    public void setCurrentAlbum(Album album) {
        this.currentAlbum = album;
        // Ensure currentUser is not null.
        if (this.currentUser == null) {
            this.currentUser = SessionManager.getCurrentUser();
        }
        // Populate the album combo box with all of the user's albums.
        if (albumComboBox != null) {
            albumComboBox.getItems().clear();
            for (Album userAlbum : currentUser.getAlbums()) {
                albumComboBox.getItems().add(userAlbum.getName());
            }
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            App.setRoot("album_details"); // Redirect to the album details view
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to return to album details view.");
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
            // Create a new photo with the file's last modified date as the date taken
            LocalDateTime dateTaken = LocalDateTime.now(); // You can improve this by actually reading the file
                                                           // attribute if desired.
            Photo newPhoto = new Photo(selectedFile.getAbsolutePath(), "", dateTaken);

            // Add the photo to the current album
            currentAlbum.addPhoto(newPhoto);

            showInfo("Photo added successfully.");
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

        // Clear UI components
        photoImageView.setImage(null);
        photoCaptionField.clear();
        tagListView.getItems().clear();
        photoDateLabel.setText("");

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

    /**
     * New handler: Save caption button could call this if separate from edit.
     * (Alternatively, handleEditCaption might suffice.)
     */
    @FXML
    private void handleSaveCaption() {
        handleEditCaption();
    }

    @FXML
   
private void handleAddTag() {
    if (selectedPhoto == null) {
        showError("No photo selected.");
        return;
    }
    String tagType = tagTypeComboBox.getValue();
    String tagValue = tagValueField.getText().trim();

    if (tagType == null || tagType.isEmpty()) {
        showError("Please select a tag type.");
        return;
    }
    if (tagValue.isEmpty()) {
        showError("Tag value cannot be empty.");
        return;
    }

    // Persist the new tag type in your session's set of known types.
    knownTagTypes.add(tagType);
    refreshTagTypeComboBox();

    Tag newTag = new Tag(tagType, tagValue);
    selectedPhoto.addTag(newTag);

    refreshTagList();
    tagValueField.clear();
    showInfo("Tag added: " + tagType + "=" + tagValue);
    
    // Save the updated user object so that tag changes are persisted.
    SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");
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
    // Expected format: "name=value"
    String[] parts = selectedTag.split("=");
    if (parts.length == 2) {
        Tag tagToRemove = new Tag(parts[0].trim(), parts[1].trim());
        selectedPhoto.removeTag(tagToRemove);
        refreshTagList();
        showInfo("Tag removed: " + selectedTag);
        
        // Save the updated user object so that removal is persisted.
        SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");
    }
}


    @FXML
    private void handleDefineNewTagType() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Define New Tag Type");
        dialog.setHeaderText("Add a new tag type");
        dialog.setContentText("Enter new tag type:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newTagType = result.get().trim();
            if (!newTagType.isEmpty()) {
                knownTagTypes.add(newTagType);
                refreshTagTypeComboBox();
                showInfo("New tag type added: " + newTagType);
            }
        }
    }

    private void refreshTagTypeComboBox() {
        if (tagTypeComboBox != null) {
            tagTypeComboBox.getItems().clear();
            tagTypeComboBox.getItems().addAll(knownTagTypes);
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
        currentAlbum.deletePhoto(selectedPhoto);
        destinationAlbum.addPhoto(selectedPhoto);

        // Clear display as photo is moved out.
        photoImageView.setImage(null);
        photoCaptionField.clear();
        tagListView.getItems().clear();
        photoDateLabel.setText("");

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
            try {
                Image image = new Image("file:" + photo.getFilepath());
                photoImageView.setImage(image);
            } catch (Exception e) {
                photoImageView.setImage(null);
                showError("Failed to load the image.");
            }
            photoCaptionField.setText(photo.getCaption());

            // Attempt to get the file's last modified date and display it.
            File file = new File(photo.getFilepath());
            LocalDateTime lastModified = getLastModifiedDate(file, photo.getDateTaken());
            photoDateLabel.setText("Date Taken: " + lastModified);

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

    /**
     * Reads the last modified date from the file. If it fails, falls back to the
     * provided fallback date.
     */
    private LocalDateTime getLastModifiedDate(File file, LocalDateTime fallback) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long millis = attrs.lastModifiedTime().toMillis();
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        } catch (IOException e) {
            return fallback;
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
