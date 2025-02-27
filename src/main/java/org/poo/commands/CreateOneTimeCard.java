package org.poo.commands;

import org.poo.entities.Account.Account;
import org.poo.entities.Card.OneTimeCard;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;

/**
 * Command to create a new one-time card for a specific account.
 */
public final class CreateOneTimeCard implements Command {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;

    public CreateOneTimeCard(final AccountRepository accountRepository,
                             final CardRepository cardRepository,
                             final TransactionRepository transactionRepository,
                             final CommandInput command) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
    }

    /**
     * Executes the logic to create a one-time card.
     */
    @Override
    public void execute() {
        String accountIban = command.getAccount();
        String email = command.getEmail();
        Account account = accountRepository.getAccountByIban(accountIban);

        // Create one time card
        OneTimeCard newCard = new OneTimeCard(accountIban, email);
        cardRepository.saveCard(newCard);

        Transaction transaction = new Transaction(
                email,
                accountIban,
                accountIban,
                0.0,
                account.getCurrency(),
                command.getTimestamp(),
                "New card created",
                TransactionType.CREATE_CARD,
                newCard.getCardNumber(),
                null,
                null
        );

        transactionRepository.recordTransaction(transaction);
    }
}
