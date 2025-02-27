package org.poo.services;

import org.poo.entities.User;
import org.poo.repository.UserRepository;

import java.util.List;

/**
 * Service class for managing users.
 */
public final class UserService {
    private final UserRepository userRepository;

    /**
     * Constructor to initialize UserService with a user repository.
     *
     * @param userRepository the repository for managing users.
     */
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Adds a new user to the system.
     *
     * @param firstName the user's first name.
     * @param lastName  the user's last name.
     * @param email     the user's email address.
     * @param birthDate the user's birth date.
     * @param occupation the user's occupation.
     * @throws IllegalArgumentException if a user with the given email already exists.
     */
    public void addUser(final String firstName, final String lastName,
                        final String email, final String birthDate, final String occupation) {
        if (userRepository.userExists(email)) {
            throw new IllegalArgumentException("User already exists with email: " + email);
        }
        User newUser = new User(firstName, lastName, email, birthDate, occupation);
        userRepository.addUser(newUser);
    }

    /**
     * Returns a list of all users in the system.
     *
     * @return a list of all users.
     */
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email the email of the user to retrieve.
     * @return the user associated with the given email.
     * @throws IllegalArgumentException if no user is found with the given email.
     */
    public User getUserByEmail(final String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }
        return user;
    }

    /**
     * Deletes a user by their email.
     *
     * @param email the email of the user to delete.
     * @throws IllegalArgumentException if no user is found with the given email.
     */
    public void deleteUserByEmail(final String email) {
        if (!userRepository.userExists(email)) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }
        userRepository.deleteUser(email);
    }
}
