package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    // Optional: If we decide to add a password field:
    // @FXML
    // private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Please enter a username.");
            return;
        }

        // Here you can add logic to verify credentials and switch views.
        if (username.equalsIgnoreCase("admin")) {
            // For an admin user, you might load an admin view.
            showInfo("Admin login successful.");
            // Example: App.setRoot("admin");
        } else {
            // For normal users
            showInfo("User login successful.");
            // Example: App.setRoot("primary");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
