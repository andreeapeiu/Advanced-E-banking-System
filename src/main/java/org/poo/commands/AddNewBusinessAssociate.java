package org.poo.commands;

import org.poo.entities.Account.BusinessAccount;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.UserRepository;

/**
 * Command class to add a new associate to a business account.
 */
public class AddNewBusinessAssociate implements Command {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CommandInput command;

    /**
     * Constructor to initialize repositories and command input.
     *
     * @param accountRepository Repository for managing accounts.
     * @param userRepository    Repository for managing users.
     * @param command           Command input containing account and associate details.
     */
    public AddNewBusinessAssociate(final AccountRepository accountRepository,
                                   final UserRepository userRepository,
                                   final CommandInput command) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.command = command;
    }

    /**
     * Executes the command to add a new business associate.
     * - Validates the business account and user permissions.
     * - Checks if the user exists in the system.
     * - Adds the user as an associate with the specified role.
     */
    @Override
    public void execute() {
        final String accountIban = command.getAccount();
        final String role = command.getRole();
        final String email = command.getEmail();

        final BusinessAccount businessAccount =
                (BusinessAccount) accountRepository.findAccountByIban(accountIban);

        if (businessAccount == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        // Verify that the account is a business account.
        if (!businessAccount.getAccountType().equals("business")) {
            throw new IllegalArgumentException("Account is not of type business.");
        }

        // Validate if the requesting user is owner or manager
        final String requestingUserEmail = businessAccount.getOwnerEmail();
        final String requesterRole = businessAccount.getRole(requestingUserEmail);

        if (!requestingUserEmail.equalsIgnoreCase(businessAccount.getOwnerEmail())
                && !"manager".equalsIgnoreCase(requesterRole)) {
            throw new IllegalArgumentException("You are not authorized to make this transaction.");
        }

        // Check if the user to be added exists in the system.
        final User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        // Add the user as an associate with the specified role.
        try {
            businessAccount.addAssociate(email, role);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
