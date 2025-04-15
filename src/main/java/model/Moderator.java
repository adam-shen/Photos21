package model;

/**
 * Moderator represents a user with extra privileges, such as moderating albums.
 * Inherits from User and adds moderation functionality.
 * 
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
public class Moderator extends User {

    /**
     * Constructs a Moderator with the specified username.
     *
     * @param username the username of the moderator
     */
    public Moderator(String username) {
        super(username);
    }

    /**
     * Moderates the specified album.
     * (Insert moderation logic as required by your application.)
     *
     * @param album the album to moderate
     */
    public void moderateAlbum(Album album) {
        // Example implementation: simply print out a moderation message.
        System.out.println("Moderator " + username + " is moderating album: " + album.getName());
        // Additional moderation logic (e.g., reviewing photos, flagging content) can be
        // implemented here.
    }
}
