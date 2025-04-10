package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import app.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.Tag;
import model.User;
import util.SerializationUtil;

public class AlbumController {

    @FXML
    private ListView<Album> albumListView;

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

    private User currentUser;

    @FXML
    private void initialize() {
        // Get the current user
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            refreshAlbumList();
        }
    }

    @FXML
    private void handleCreateAlbum() {
        String albumName = albumNameField.getText().trim();
        if (albumName.isEmpty()) {
            showError("Album name cannot be empty.");
            return;
        }

        // Check if album with same name already exists
        for (Album album : currentUser.getAlbums()) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                showError("An album with this name already exists.");
                return;
            }
        }

        // Create and add the album
        Album newAlbum = new Album(albumName);
        currentUser.addAlbum(newAlbum);

        // Update UI
        refreshAlbumList();
        albumNameField.clear();

        // Save user data
        saveUserData();

        showInfo("Album '" + albumName + "' created successfully.");
    }

    @FXML
    private void handleDeleteAlbum() {
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to delete.");
            return;
        }

        // Remove the album
        currentUser.removeAlbum(selectedAlbum);

        // Update UI
        refreshAlbumList();

        // Save user data
        saveUserData();

        showInfo("Album '" + selectedAlbum.getName() + "' deleted successfully.");
    }

    @FXML
    private void handleRenameAlbum() {
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

            // Check if album with new name already exists
            for (Album album : currentUser.getAlbums()) {
                if (!album.equals(selectedAlbum) && album.getName().equalsIgnoreCase(newName)) {
                    showError("An album with this name already exists.");
                    return;
                }
            }

            // Rename the album
            selectedAlbum.renameAlbum(newName);

            // Update UI
            refreshAlbumList();

            // Save user data
            saveUserData();

            showInfo("Album renamed to '" + newName + "' successfully.");
        }
    }

    @FXML
    private void handleOpenAlbum() {
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError("Please select an album to open.");
            return;
        }

        try {
            // Load the photo view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photo_view.fxml"));
            Scene photoScene = new Scene(loader.load());

            // Get the controller and set the album
            PhotoController photoController = loader.getController();
            photoController.setCurrentAlbum(selectedAlbum);

            // Create a new stage for the photo view
            Stage photoStage = new Stage();
            photoStage.setTitle("Photos - " + selectedAlbum.getName());
            photoStage.setScene(photoScene);
            photoStage.show();
        } catch (IOException e) {
            showError("Failed to open album view.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchByDate() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showError("Please select both start and end dates.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showError("Start date must be before or equal to end date.");
            return;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Search for photos within the date range across all albums
        ArrayList<Photo> searchResults = new ArrayList<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                LocalDateTime photoDate = photo.getDateTaken();
                if ((photoDate.isEqual(startDateTime) || photoDate.isAfter(startDateTime)) &&
                        (photoDate.isEqual(endDateTime) || photoDate.isBefore(endDateTime))) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }

        displaySearchResults(searchResults, "Date Search: " + startDate + " to " + endDate);
    }

    @FXML
    private void handleSearchByTag() {
        String tagType = tagTypeField.getText().trim();
        String tagValue = tagValueField.getText().trim();

        if (tagType.isEmpty() || tagValue.isEmpty()) {
            showError("Please enter tag type and value.");
            return;
        }

        // Create the tag to search for
        Tag searchTag = new Tag(tagType, tagValue);

        // Search for photos with the tag across all albums
        ArrayList<Photo> searchResults = new ArrayList<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> photoTags = photo.getTags();
                if (photoTags.contains(searchTag)) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }

        displaySearchResults(searchResults, "Tag Search: " + tagType + "=" + tagValue);
    }

    @FXML
    private void handleSearchByTagAND() {
        String tagType1 = tagTypeField.getText().trim();
        String tagValue1 = tagValueField.getText().trim();
        String tagType2 = secondTagTypeField.getText().trim();
        String tagValue2 = secondTagValueField.getText().trim();

        if (tagType1.isEmpty() || tagValue1.isEmpty() || tagType2.isEmpty() || tagValue2.isEmpty()) {
            showError("Please enter both tag types and values.");
            return;
        }

        // Create the tags to search for
        Tag searchTag1 = new Tag(tagType1, tagValue1);
        Tag searchTag2 = new Tag(tagType2, tagValue2);

        // Search for photos with both tags across all albums
        ArrayList<Photo> searchResults = new ArrayList<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> photoTags = photo.getTags();
                if (photoTags.contains(searchTag1) && photoTags.contains(searchTag2)) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }

        displaySearchResults(searchResults, "Tag AND Search: " + tagType1 + "=" + tagValue1 + " AND " +
                tagType2 + "=" + tagValue2);
    }

    @FXML
    private void handleSearchByTagOR() {
        String tagType1 = tagTypeField.getText().trim();
        String tagValue1 = tagValueField.getText().trim();
        String tagType2 = secondTagTypeField.getText().trim();
        String tagValue2 = secondTagValueField.getText().trim();

        if (tagType1.isEmpty() || tagValue1.isEmpty() || tagType2.isEmpty() || tagValue2.isEmpty()) {
            showError("Please enter both tag types and values.");
            return;
        }

        // Create the tags to search for
        Tag searchTag1 = new Tag(tagType1, tagValue1);
        Tag searchTag2 = new Tag(tagType2, tagValue2);

        // Search for photos with either tag across all albums
        ArrayList<Photo> searchResults = new ArrayList<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> photoTags = photo.getTags();
                if (photoTags.contains(searchTag1) || photoTags.contains(searchTag2)) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }

        displaySearchResults(searchResults, "Tag OR Search: " + tagType1 + "=" + tagValue1 + " OR " +
                tagType2 + "=" + tagValue2);
    }

    private void displaySearchResults(ArrayList<Photo> searchResults, String searchDescription) {
        if (searchResults.isEmpty()) {
            showInfo("No photos found matching the search criteria.");
            return;
        }

        // Ask the user if they want to create an album with the search results
        TextInputDialog dialog = new TextInputDialog("Search Results");
        dialog.setTitle("Search Results");
        dialog.setHeaderText("Found " + searchResults.size() + " photo(s)");
        dialog.setContentText("Enter album name to save results (leave empty to cancel):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String albumName = result.get().trim();

            // Check if album with this name already exists
            for (Album album : currentUser.getAlbums()) {
                if (album.getName().equalsIgnoreCase(albumName)) {
                    showError("An album with this name already exists.");
                    return;
                }
            }

            // Create a new album with the search results
            Album newAlbum = new Album(albumName);
            for (Photo photo : searchResults) {
                newAlbum.addPhoto(photo);
            }

            // Add the album to the user's albums
            currentUser.addAlbum(newAlbum);

            // Update UI
            refreshAlbumList();

            // Save user data
            saveUserData();

            showInfo("Created album '" + albumName + "' with " + searchResults.size() + " photo(s).");
        } else {
            // Just show the number of results without creating an album
            showInfo("Found " + searchResults.size() + " photo(s) matching your search.");
        }
    }

    @FXML
    private void handleLogout() {
        // Save user data before logout
        saveUserData();

        try {
            App.setRoot("login");
        } catch (IOException e) {
            showError("Failed to return to login screen.");
            e.printStackTrace();
        }
    }

    private void refreshAlbumList() {
        if (albumListView != null) {
            albumListView.getItems().clear();
            albumListView.getItems().addAll(currentUser.getAlbums());
        }
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