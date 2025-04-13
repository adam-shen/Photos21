package controller;

import java.io.IOException;

import app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.Album;
import model.User;
import util.SerializationUtil;

public class LoginController {

    @FXML
    private TextField usernameField;

    // Optional password field if you decide to implement it
    // @FXML
    // private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Please enter a username.");
            return;
        }

        if (username.equalsIgnoreCase("admin")) {
            // For admin user, navigate to admin view
            try {
                App.setRoot("admin");
            } catch (IOException e) {
                showError("Failed to load the admin view.");
                e.printStackTrace();
            }
        } else if (username.equalsIgnoreCase("stock")) {
            // For stock user, navigate to primary view with stock photos
            try {
                // Load or create the stock user
                User stockUser = SerializationUtil.load("data/users/stock.dat");
                if (stockUser == null) {
                    stockUser = new User("stock");
                    // Initialize with stock albums if needed
                    SerializationUtil.save(stockUser, "data/users/stock.dat");
                }

                // Set the current user in some session management
                SessionManager.setCurrentUser(stockUser);

                App.setRoot("primary");
            } catch (IOException e) {
                showError("Failed to load the stock user view.");
                e.printStackTrace();
            }
        } else {
            // For regular users, check if they exist
            User user = SerializationUtil.load("data/users/" + username + ".dat");

            if (user == null) {
                showError("User doesn't exist. Please try again or contact admin.");
                return;
            }

            try {
                // Set the current user in session management
                SessionManager.setCurrentUser(user);

                App.setRoot("primary");
            } catch (IOException e) {
                showError("Failed to load the primary view.");
                e.printStackTrace();
            }
        }

        File dataDir = new File("data/users");
if (!dataDir.exists()) {
    dataDir.mkdirs();
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

// A simple session management class to track the current user
class SessionManager {
    private static User currentUser;
    private static Album currentAlbum;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentAlbum(Album album) {
        currentAlbum = album;
    }

    public static Album getCurrentAlbum() {
        return currentAlbum;
    }
}