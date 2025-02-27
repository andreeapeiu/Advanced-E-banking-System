package org.poo.services;

import org.poo.entities.Card.Card;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.SpendingsRepository;

import java.util.List;

/**
 * Service class for managing card operations.
 * This class provides functionality to interact with cards associated with accounts.
 */
public final class CardService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SpendingsRepository spendingsRepository;

    /**
     * Constructor for initializing the CardService with necessary repositories.
     *
     * @param accountRepository the repository for accounts
     * @param cardRepository    the repository for cards
     * @param userRepository    the repository for users
     * @param transactionRepository the repository for transactions
     * @param spendingsRepository the repository for spendings
     */
    public CardService(final AccountRepository accountRepository,
                       final CardRepository cardRepository,
                       final UserRepository userRepository,
                       final TransactionRepository transactionRepository,
                       final SpendingsRepository spendingsRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.spendingsRepository = spendingsRepository;
    }

    /**
     * Retrieves a list of cards associated with the given account IBAN.
     *
     * @param accountIban the IBAN of the account
     * @return a list of cards associated with the provided IBAN
     */
    public List<Card> getCards(final String accountIban) {
        return cardRepository.getCardsByAccount(accountIban);
    }
}
