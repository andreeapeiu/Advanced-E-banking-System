package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;

/**
 * Command to delete a card for a specific account.
 */
public final class DeleteCard implements Command {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public DeleteCard(final AccountRepository accountRepository,
                      final CardRepository cardRepository,
                      final TransactionRepository transactionRepository,
                      final CommandInput command,
                      final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the logic to delete a card.
     */
    @Override
    public void execute() {
        String number = command.getCardNumber();
        Card card = cardRepository.getCardByNumber(number);

        if (card == null) {
            return;
        }

        cardRepository.deleteCard(number);

        // Extract the data for transaction
        String accountIban = card.getAccountIban();
        Account account = accountRepository.getAccountByIban(accountIban);

        Transaction transaction = new Transaction(
                command.getEmail(),
                accountIban,
                accountIban,
                0.0,
                account.getCurrency(),
                command.getTimestamp(),
                "The card has been destroyed",
                TransactionType.DELETE_CARD,
                number,
                null,
                null
        );

        transactionRepository.recordTransaction(transaction);
    }
}
