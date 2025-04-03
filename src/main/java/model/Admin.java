
import java.util.ArrayList;

/**
 * Admin is a special class that handles user management functions such as
 * listing users, creating a new user, and deleting an existing user.
 * This class is not a subclass of User since it represents an administrative role.
 * 
 * @author YourName
 */
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
        // Optionally, check for duplicate users by username.
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
