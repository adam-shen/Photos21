/**
 * Admin is a special class that handles user management functions such as
 * listing users, creating a new user, and deleting an existing user.
 * This class is not a subclass of User since it represents an administrative
 * role within the Photos application.
 * 
 * <p>
 * This class interacts with the underlying persistence mechanism to save and
 * load user data from disk. It is used by the AdminController to display and manage
 * user accounts.
 * </p>
 * 
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */

package model;

import java.util.ArrayList;

public class Admin {
    private ArrayList<User> users;

    /**
     * Constructs an Admin instance.
     */
    public Admin() {
        this.users = new ArrayList<>();
    }

    /**
     * Returns a list of all users.
     *
     * @return an ArrayList of User objects
     */
    public ArrayList<User> listUsers() {
        System.out.println("Listing users:");
        for (User user : users) {
            System.out.println(" - " + user.getUsername());
        }
        return users;
    }

    /**
     * Adds a new user.
     *
     * @param user the user to create
     */
    public void createUser(User user) {

        if (!users.contains(user)) {
            users.add(user);
            System.out.println("User \"" + user.getUsername() + "\" created.");
        } else {
            System.out.println("User \"" + user.getUsername() + "\" already exists.");
        }
    }

    /**
     * Deletes an existing user.
     *
     * @param user the user to delete
     */
    public void deleteUser(User user) {
        if (users.remove(user)) {
            System.out.println("User \"" + user.getUsername() + "\" deleted.");
        } else {
            System.out.println("User \"" + user.getUsername() + "\" not found.");
        }
    }
}
