package org.poo.services;

import org.poo.entities.Account.Account;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.AliasRepository;
import org.poo.entities.User;

import java.util.List;

/**
 * Service class for managing accounts. This class provides methods to retrieve accounts
 * associated with a specific user based on their email address.
 */
public final class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final AliasRepository aliasRepository;

    /**
     * Constructor for AccountService.
     *
     * @param accountRepository   the repository for accounts.
     * @param transactionRepository the repository for transactions.
     * @param userRepository      the repository for users.
     * @param cardRepository      the repository for cards.
     * @param aliasRepository     the repository for aliases.
     */
    public AccountService(final AccountRepository accountRepository,
                          final TransactionRepository transactionRepository,
                          final UserRepository userRepository,
                          final CardRepository cardRepository,
                          final AliasRepository aliasRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.aliasRepository = aliasRepository;
    }

    /**
     * Retrieves the accounts associated with a given user's email.
     *
     * @param email the email address of the user.
     * @return a list of accounts associated with the user.
     * @throws IllegalArgumentException if the user is not found.
     */
    public List<Account> getAccounts(final String email) {
        User user = userRepository.findUserByEmail(email); // Uses UserRepository to find the user
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user.getAccounts();
    }
}
