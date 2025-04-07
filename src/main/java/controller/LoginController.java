package controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    // Optional: If we implement a password field, add:
    // @FXML
    // private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showError("Please enter a username.");
            return;
        }

        // Optional: Check for a password here if needed

        try {
            FXMLLoader loader;
            Parent root;
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Check for the admin user
            if (username.equalsIgnoreCase("admin")) {
                loader = new FXMLLoader(getClass().getResource("AdminScreen.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("PrimaryScreen.fxml"));
            }

            root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the next screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
