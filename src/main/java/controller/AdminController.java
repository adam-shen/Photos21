package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AdminController {

    @FXML
    private void initialize() {
        // Initialization code, e.g. loading user list, goes here.
    }

    @FXML
    private void handleCreateUser() {
        // Logic to create a new user
        showInfo("Create User functionality is not implemented yet.");
    }

    @FXML
    private void handleDeleteUser() {
        // Logic to delete an existing user
        showInfo("Delete User functionality is not implemented yet.");
    }

    @FXML
    private void handleListUsers() {
        // Logic to list all users in the admin view
        showInfo("List Users functionality is not implemented yet.");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
