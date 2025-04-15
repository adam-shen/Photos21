package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import app.Photos;
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

    @FXML
    private ComboBox<String> albumComboBox;

    // Maintains search results
    private List<Photo> searchResults = new ArrayList<>();

    private User currentUser;

    @FXML
    private void initialize() {
        System.out.println("SearchController initialized!");
        currentUser = SessionManager.getCurrentUser();
        albumComboBox.getItems().clear();
        for (Album album : currentUser.getAlbums()) {
            albumComboBox.getItems().add(album.getName());
        }
        // Set a custom cell factory (if desired) for displaying image thumbnails, etc.
        searchResultsListView.setCellFactory(listView -> new javafx.scene.control.ListCell<Photo>() {
            private javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();

            @Override
            protected void updateItem(Photo photo, boolean empty) {
                super.updateItem(photo, empty);
                if (empty || photo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(photo.getCaption());
                    try {
                        javafx.scene.image.Image thumbnail = new javafx.scene.image.Image("file:" + photo.getFilepath(),
                                60, 60, true, true);
                        imageView.setImage(thumbnail);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    @FXML
    private void performDateRangeSearch() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both start and end dates.");
            return;
        }
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
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
            for (Album album : currentUser.getAlbums()) {
                if (album.getName().equals(albumComboBox.getValue())) {
                    photosToSearch.addAll(album.getPhotos());
                    break;
                }
            }
        } else {
            for (Album album : currentUser.getAlbums()) {
                photosToSearch.addAll(album.getPhotos());
            }
        }

        for (Photo photo : photosToSearch) {
            boolean matches = false;
            if (operator == null) {
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
        String newAlbumName = "Search Results Album";
        Album newAlbum = new Album(newAlbumName);
        for (Photo photo : searchResults) {
            newAlbum.addPhoto(photo);
        }
        currentUser.addAlbum(newAlbum);
        showInfo("New album '" + newAlbumName + "' created with " + searchResults.size() + " photos.");
    }

    @FXML
    private void handleBack() {
        try {
            Photos.setRoot("primary"); // Change "primary" if your main album view is named differently
        } catch (IOException e) {
            e.printStackTrace();
            showError("Unable to return to the main album list.");
        }
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
