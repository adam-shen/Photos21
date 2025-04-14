package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.Album;
import model.Photo;
import model.Tag;
import model.User;

public class SearchController {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField tagQueryField; // e.g., "person=alice" or "person=alice AND location=paris"

    @FXML
    private ListView<Photo> searchResultsListView;

    // In case you want the user to select the album to search in via a ComboBox:
    @FXML
    private ComboBox<String> albumComboBox;
    
    // Maintains search results
    private List<Photo> searchResults = new ArrayList<>();
    
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getCurrentUser();
        albumComboBox.getItems().clear();
        for (Album album : currentUser.getAlbums()) {
            albumComboBox.getItems().add(album.getName());
        }
    }

    @FXML
    private void performDateRangeSearch() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both start and end dates.");
            return;
        }
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        // Convert dates to LocalDateTime (start of day & end of day)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        searchResults.clear();
        List<Photo> photosToSearch = new ArrayList<>();
        if (albumComboBox.getValue() != null) {
            // Search in a specific album:
            for (Album album : currentUser.getAlbums()) {
                if (album.getName().equals(albumComboBox.getValue())) {
                    photosToSearch.addAll(album.getPhotos());
                    break;
                }
            }
        } else {
            // Search across all albums
            for (Album album : currentUser.getAlbums()) {
                photosToSearch.addAll(album.getPhotos());
            }
        }
        for (Photo photo : photosToSearch) {
            LocalDateTime photoDate = photo.getDateTaken();
            if (!photoDate.isBefore(startDateTime) && !photoDate.isAfter(endDateTime)) {
                searchResults.add(photo);
            }
        }
        updateSearchResultsView();
    }

    @FXML
    private void performTagSearch() {
        String query = tagQueryField.getText().trim();
        if (query.isEmpty()) {
            showError("Please enter a tag query.");
            return;
        }
        searchResults.clear();
        
        // Simple parser that supports at most two conditions with AND/OR
        String operator = null;
        String[] parts = null;
        if (query.contains(" AND ")) {
            operator = "AND";
            parts = query.split(" AND ");
        } else if (query.contains(" OR ")) {
            operator = "OR";
            parts = query.split(" OR ");
        } else {
            parts = new String[] { query };
        }
        
        List<Tag> tagConditions = new ArrayList<>();
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length != 2) {
                showError("Invalid tag format. Use tag=value.");
                return;
            }
            String key = kv[0].trim();
            String value = kv[1].trim();
            tagConditions.add(new Tag(key, value));
        }
        
        List<Photo> photosToSearch = new ArrayList<>();
        if (albumComboBox.getValue() != null) {
            // Search only in the selected album:
            for (Album album : currentUser.getAlbums()) {
                if (album.getName().equals(albumComboBox.getValue())) {
                    photosToSearch.addAll(album.getPhotos());
                    break;
                }
            }
        } else {
            // Otherwise, search across all albums
            for (Album album : currentUser.getAlbums()) {
                photosToSearch.addAll(album.getPhotos());
            }
        }
        
        for (Photo photo : photosToSearch) {
            boolean matches = false;
            if (operator == null) {
                // Single condition
                matches = photo.getTags().contains(tagConditions.get(0));
            } else if (operator.equals("AND")) {
                matches = photo.getTags().containsAll(tagConditions);
            } else if (operator.equals("OR")) {
                for (Tag condition : tagConditions) {
                    if (photo.getTags().contains(condition)) {
                        matches = true;
                        break;
                    }
                }
            }
            if (matches) {
                searchResults.add(photo);
            }
        }
        updateSearchResultsView();
    }

    private void updateSearchResultsView() {
        searchResultsListView.getItems().clear();
        searchResultsListView.getItems().addAll(searchResults);
        if (searchResults.isEmpty()) {
            showInfo("No matching photos found.");
        } else {
            showInfo(searchResults.size() + " photos found.");
        }
    }

    @FXML
    private void handleCreateAlbumFromSearchResults() {
        if (searchResults.isEmpty()) {
            showError("No photos in search results to create an album.");
            return;
        }
        
        // Normally, prompt the user for an album name (e.g., using TextInputDialog)
        // For simplicity, we'll use a fixed album name or you can integrate a dialog here.
        String newAlbumName = "Search Results Album";
        Album newAlbum = new Album(newAlbumName);
        // Copy the photo references into the new album (no duplication of the actual photo data)
        for (Photo photo : searchResults) {
            newAlbum.addPhoto(photo);
        }
        currentUser.addAlbum(newAlbum);
        showInfo("New album '" + newAlbumName + "' created with " + searchResults.size() + " photos.");
        
        //SerializationUtil.save(currentUser, "data/users/" + currentUser.getUsername() + ".dat");
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Search Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Search Info");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
