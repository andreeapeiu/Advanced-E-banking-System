package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;
import org.poo.entities.Card.CardStatus;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;

public final class CheckCardStatus implements Command {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public CheckCardStatus(final AccountRepository accountRepository,
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

    @Override
    public void execute() {
        String number = command.getCardNumber();
        Card card = cardRepository.getCardByNumber(number);
        if (card == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode out = mapper.createObjectNode();
            out.put("command", "checkCardStatus");

            ObjectNode out2 = mapper.createObjectNode();
            out2.put("description", "Card not found");
            out.put("timestamp", command.getTimestamp());
            out2.put("timestamp", command.getTimestamp());

            out.set("output", out2);
            output.add(out);
            return;
        }

        // Find the account by iban
        Account account = accountRepository.getAccountByIban(card.getAccountIban());
        String email = account.getOwner().getEmail();

        // Verify account balance
        if (account.getBalance() == 0) {
            Transaction transaction = new Transaction(
                    email,
                    account.getIban(),
                    account.getIban(),
                    0.0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "You have reached the minimum amount of funds, the card will be frozen",
                    TransactionType.CHECK_CARD_STATUS,
                    number,
                    null,
                    null
            );
            transactionRepository.recordTransaction(transaction);

            // Freeze the card
            card.setStatus(CardStatus.frozen);
            return;
        }

        // Verify if I had set the account's minimum balance
        if (!account.getHasMinimumBalance()) {
            return;
        }

        if (account.getBalance() < account.getMinimumBalance()
                && card.getStatus() != CardStatus.frozen) {
            // Freeze the card
            card.setStatus(CardStatus.frozen);

            Transaction transaction = new Transaction(
                    email,
                    account.getIban(),
                    account.getIban(),
                    0.0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "You have reached the minimum amount of funds, the card will be frozen",
                    TransactionType.CHECK_CARD_STATUS,
                    number,
                    null,
                    null
            );
            transactionRepository.recordTransaction(transaction);
        }

        if (card.getStatus().equals(CardStatus.frozen)) {
            // Set the status to frozen
            Transaction transaction = new Transaction(
                    email,
                    account.getIban(),
                    account.getIban(),
                    0.0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "The card is frozen",
                    TransactionType.CHECK_CARD_STATUS,
                    number,
                    null,
                    null
            );
        }

    }
}

