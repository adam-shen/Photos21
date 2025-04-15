package model;

/**
 * Member represents a regular user of the application.
 * It inherits all functionality from the User class.
 * 
 * (Additional member-specific methods can be added here.)
 * 
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
public class Member extends User {

    /**
     * Constructs a Member with the specified username.
     *
     * @param username the username of the member
     */
    public Member(String username) {
        super(username);
    }
}
