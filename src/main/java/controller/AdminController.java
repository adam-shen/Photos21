package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import app.Photos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import model.Admin;
import model.User;
import util.SerializationUtil;

public class AdminController {

    @FXML
    private ListView<String> userListView;

    private Admin adminModel;
    private ObservableList<String> usernames;

    @FXML
    private void initialize() {
        // Create Admin model
        adminModel = new Admin();

        // Load existing users from data directory
        loadUsers();

        // Set up ListView with usernames
        usernames = FXCollections.observableArrayList();
        refreshUserList();
        userListView.setItems(usernames);
    }

    @FXML
    private void handleCreateUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create User");
        dialog.setHeaderText("Create a new user");
        dialog.setContentText("Enter username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String username = result.get().trim();

            // Check if user already exists
            if (userExists(username)) {
                showError("User '" + username + "' already exists.");
                return;
            }

            // Create new user and save
            User newUser = new User(username);
            adminModel.createUser(newUser);
            SerializationUtil.save(newUser, "data/users/" + username + ".dat");

            // Refresh the list view
            refreshUserList();
            showInfo("User '" + username + "' created successfully.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        String selectedUsername = userListView.getSelectionModel().getSelectedItem();
        if (selectedUsername == null) {
            showError("Please select a user to delete.");
            return;
        }

        // Don't allow deleting the stock user
        if (selectedUsername.equalsIgnoreCase("stock")) {
            showError("The stock user cannot be deleted.");
            return;
        }

        // Find and delete the user
        User userToDelete = loadUser(selectedUsername);
        if (userToDelete != null) {
            adminModel.deleteUser(userToDelete);

            // Delete user data file
            File userFile = new File("data/users/" + selectedUsername + ".dat");
            if (userFile.exists()) {
                userFile.delete();
            }

            // Refresh the list view
            refreshUserList();
            showInfo("User '" + selectedUsername + "' deleted successfully.");
        }
    }

    @FXML
    private void handleListUsers() {
        // The users are already listed in the ListView
        // This is just a convenience method that could log or perform additional
        // actions
        ArrayList<User> users = adminModel.listUsers();
        showInfo("Users listed in the view. Total count: " + users.size());
    }

    @FXML
    private void handleLogout() {
        try {
            Photos.setRoot("login");
        } catch (IOException e) {
            showError("Failed to return to login screen.");
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        // Create data directory if it doesn't exist
        File dataDir = new File("data/users");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Load existing users
        File[] userFiles = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
        if (userFiles != null) {
            for (File file : userFiles) {
                String username = file.getName().replace(".dat", "");
                User user = SerializationUtil.load(file.getPath());
                if (user != null) {
                    adminModel.createUser(user);
                }
            }
        }
    }

    private User loadUser(String username) {
        return SerializationUtil.load("data/users/" + username + ".dat");
    }

    private boolean userExists(String username) {
        File userFile = new File("data/users/" + username + ".dat");
        return userFile.exists();
    }

    private void refreshUserList() {
        // Reinitialize the admin model so that stale data is cleared.
        adminModel = new Admin();
        // Reload users from disk (which will only load files that still exist)
        loadUsers();
        // Clear the observable list and repopulate it
        usernames.clear();
        ArrayList<User> users = adminModel.listUsers();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Admin Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}