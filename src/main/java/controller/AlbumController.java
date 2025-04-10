package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AlbumController {

    // Assume a TextField from an album creation dialog
    @FXML
    private TextField albumNameField;

    @FXML
    private void handleCreateAlbum() {
        String albumName = albumNameField.getText().trim();
        if (albumName.isEmpty()) {
            showError("Album name cannot be empty.");
            return;
        }
        // Logic to create an album (e.g. updating a user’s album list) goes here.
        showInfo("Album '" + albumName + "' created successfully.");
    }

    @FXML
    private void handleDeleteAlbum() {
        // Logic to delete the selected album goes here.
        showInfo("Delete Album functionality is not implemented yet.");
    }

    @FXML
    private void handleRenameAlbum() {
        // Logic to rename the selected album goes here.
        showInfo("Rename Album functionality is not implemented yet.");
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
