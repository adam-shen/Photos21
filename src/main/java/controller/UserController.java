package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class UserController {

    @FXML
    private void initialize() {
        // Code to initialize user view (e.g., loading the user’s albums) goes here.
    }

    @FXML
    private void handleLogout() {
        // Logic to logout the user
        showInfo("Logout functionality is not implemented yet.");
        // Example: App.setRoot("login");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
