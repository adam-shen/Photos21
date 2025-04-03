import java.io.Serializable;
import java.util.ArrayList;

/**
 * The base User class representing a generic user of the application.
 * It holds a username and a list of albums.
 * Implements Serializable for persistence.
 * 
 * @author YourName
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String username;
    protected ArrayList<Album> albums;
    
    /**
     * Constructs a User with the specified username.
     *
     * @param username the username of the user
     */
    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
    }
    
    /**
     * Adds an album to the user's album list.
     *
     * @param album the album to add
     */
    public void addAlbum(Album album) {
        // Optionally, check for duplicates here.
        if (!albums.contains(album)) {
            albums.add(album);
            System.out.println("Album \"" + album.getName() + "\" added to user " + username + ".");
        } else {
            System.out.println("Album \"" + album.getName() + "\" already exists for user " + username + ".");
        }
    }
    
    /**
     * Removes an album from the user's album list.
     *
     * @param album the album to remove
     */
    public void removeAlbum(Album album) {
        if (albums.remove(album)) {
            System.out.println("Album \"" + album.getName() + "\" removed from user " + username + ".");
        } else {
            System.out.println("Album \"" + album.getName() + "\" not found for user " + username + ".");
        }
    }
    
    /**
     * Returns the list of albums owned by the user.
     *
     * @return an ArrayList of Album objects
     */
    public ArrayList<Album> getAlbums() {
        return albums;
    }
    
    /**
     * Returns the username of the user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}