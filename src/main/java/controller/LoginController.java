/**
 * LoginController handles the user authentication for the Photos application.
 * 
 * <p>
 * This controller validates user input from the login screen, distinguishes
 * between regular, stock, and admin users, and loads or creates user data accordingly.
 * For the stock user, it also ensures that a stock album is created and populated with
 * images from the designated stock folder. The controller then navigates to the appropriate view
 * based on the user type.
 * </p>
 * 
 * <p>
 * Session management is handled via a simple inner SessionManager class that tracks the current
 * user and currently selected album.
 * </p>
 * 
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */

package controller;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import app.Photos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.Album;
import model.Photo;
import model.User;
import util.SerializationUtil;

public class LoginController {

    @FXML
    private TextField usernameField;

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
                Photos.setRoot("admin");
            } catch (IOException e) {
                showError("Failed to load the admin view.");
                e.printStackTrace();
            }
        } else if (username.equalsIgnoreCase("stock")) {
            // For stock user, load/create stock user and ensure stock album is set up with
            // the stock photos
            try {
                // Load or create the stock user from disk.
                User stockUser = (User) SerializationUtil.load("data/users/stock.dat");
                if (stockUser == null) {
                    stockUser = new User("stock");
                }

                // Check if the stock album exists.
                Album stockAlbum = null;
                for (Album album : stockUser.getAlbums()) {
                    if (album.getName().equalsIgnoreCase("stock")) {
                        stockAlbum = album;
                        break;
                    }
                }

                // If the stock album is missing, create it and load stock images.
                if (stockAlbum == null) {
                    stockAlbum = new Album("stock");
                    stockUser.addAlbum(stockAlbum);

                    // Set up the stock folder. All stock images should reside in this folder.
                    File stockFolder = new File("data/stock");
                    if (stockFolder.exists() && stockFolder.isDirectory()) {
                        // List common image formats.
                        File[] imageFiles = stockFolder
                                .listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));
                        if (imageFiles != null) {
                            for (File imageFile : imageFiles) {
                                // Use the file's last modified time as the photo's date.
                                LocalDateTime dateTaken = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(imageFile.lastModified()),
                                        ZoneId.systemDefault());
                                // Create a new Photo with the file path, empty caption, and date taken.
                                Photo photo = new Photo(imageFile.getAbsolutePath(), "", dateTaken);
                                stockAlbum.addPhoto(photo);
                            }
                        }
                    }
                }

                // Save the stock user data with updated album and photo information.
                SerializationUtil.save(stockUser, "data/users/stock.dat");

                // Set the current user (in session management) and navigate to the primary
                // view.
                SessionManager.setCurrentUser(stockUser);
                Photos.setRoot("primary");
            } catch (IOException e) {
                showError("Failed to load the stock user view.");
                e.printStackTrace();
            }
        } else {
            // For regular users, check if they exist.
            User user = (User) SerializationUtil.load("data/users/" + username + ".dat");

            if (user == null) {
                showError("User doesn't exist. Please try again or contact admin.");
                return;
            }

            try {
                // Set the current user in session management.
                SessionManager.setCurrentUser(user);
                Photos.setRoot("primary");
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
