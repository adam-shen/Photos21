package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class PhotoController {

    @FXML
    private TextField photoCaptionField;

    @FXML
    private void handleAddPhoto() {
        // Example: Open a file chooser and then add the selected photo.
        showInfo("Add Photo functionality is not implemented yet.");
    }

    @FXML
    private void handleDeletePhoto() {
        // Logic to delete a photo from an album goes here.
        showInfo("Delete Photo functionality is not implemented yet.");
    }

    @FXML
    private void handleEditCaption() {
        String newCaption = photoCaptionField.getText().trim();
        if (newCaption.isEmpty()) {
            showError("Caption cannot be empty.");
            return;
        }
        // Logic to update the photo’s caption goes here.
        showInfo("Caption updated to: " + newCaption);
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
