package org.poo.repository;

import org.poo.entities.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing user records.
 * This class is not designed for subclassing. If subclassing is necessary,
 * consider making specific methods extensible instead.
 */
public final class UserRepository {
    private final List<User> users;

    /**
     * Constructs a new UserRepository with an empty list of users.
     */
    public UserRepository() {
        this.users = new ArrayList<>();
    }

    /**
     * Adds a user to the repository.
     *
     * @param user the user to be added. Cannot be null.
     */
    public void addUser(final User user) {
        users.add(user);
    }

    /**
     * Retrieves all users from the repository.
     *
     * @return a list of all users in the repository.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to find.
     * @return the user with the matching email, or null if no such user exists.
     */
    public User findUserByEmail(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Checks if a user with the given email exists in the repository.
     *
     * @param email the email address of the user to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean userExists(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a user by their email address.
     *
     * @param email the email address of the user to delete.
     * @throws IllegalArgumentException if no user is found with the given email.
     */
    public void deleteUser(final String email) {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        users.remove(user);
    }
}
