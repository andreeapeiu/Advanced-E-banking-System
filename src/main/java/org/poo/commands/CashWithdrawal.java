package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;
import org.poo.entities.Card.CardStatus;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;
import org.poo.fileio.CommandInput;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.services.ExchangeService;

/**
 * Command for handling cash withdrawals from savings accounts.
 */
public final class CashWithdrawal implements Command {
    private static final double STANDARD_COMMISSION_RATE = 0.002; // 0.2%
    private static final double SILVER_THRESHOLD_RON = 500.0; // 500 RON
    private static final double SILVER_COMMISSION_RATE = 0.001; // 0.1%

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    /**
     * Constructor for CashWithdrawal command.
     *
     * @param accountRepository     Repository for managing accounts.
     * @param cardRepository        Repository for managing cards.
     * @param transactionRepository Repository for managing transactions.
     * @param command               The command input containing withdrawal details.
     * @param output                The JSON array node to store output messages.
     */
    public CashWithdrawal(final AccountRepository accountRepository,
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
     * Executes the cash withdrawal command.
     */
    @Override
    public void execute() {
        double amount = command.getAmount();
        String cardNumber = command.getCardNumber();
        String email = command.getEmail();
        String location = command.getLocation();
        int timestamp = command.getTimestamp();

        Card card = cardRepository.getCardByNumber(cardNumber);
        if (card == null) {
            createErrorOutput("Card not found", timestamp);
            return;
        }

        Account account = accountRepository.getAccountByIban(card.getAccountIban());
        if (account == null) {
            createErrorOutput("Account not found", timestamp);
            return;
        }

        if (card.getStatus().equals(CardStatus.frozen)) {
            createErrorOutput("The card is frozen", timestamp);
            return;
        }

        double convertedAmount = amount;
        if (!account.getCurrency().equals("RON")) {
            ExchangeService exchangeService = ExchangeService.getInstance();
            convertedAmount = exchangeService.convert(amount, "RON", account.getCurrency());
        }

        if (account.getBalance() < convertedAmount) {
            Transaction transaction = new Transaction(
                    email,
                    account.getIban(),
                    account.getIban(),
                    0.0,
                    account.getCurrency(),
                    timestamp,
                    "Insufficient funds",
                    TransactionType.CASHWITHDRAWAL,
                    cardNumber,
                    location,
                    TransactionStatus.failed
            );
            transactionRepository.recordTransaction(transaction);
            return;
        }

        // Deduct the amount from the account balance
        account.setBalance(account.getBalance() - convertedAmount);

        // Calculate commission
        ExchangeService exchangeService = ExchangeService.getInstance();
        double commission = 0.0;
        double amountInRon = exchangeService.convert(convertedAmount, account.getCurrency(), "RON");

        if (account.getOwner().getPlan().equals("standard")) {
            commission = STANDARD_COMMISSION_RATE * convertedAmount;
        } else if (account.getOwner().getPlan().equals("silver")
                && amountInRon >= SILVER_THRESHOLD_RON) {
            commission = SILVER_COMMISSION_RATE * convertedAmount;
        }

        account.setBalance(account.getBalance() - commission);
        Transaction transaction = new Transaction(
                email,
                account.getIban(),
                account.getIban(),
                amount,
                command.getCurrency(),
                timestamp,
                "Cash withdrawal of " + amount,
                TransactionType.CASHWITHDRAWAL,
                cardNumber,
                location,
                TransactionStatus.successful
        );
        transactionRepository.recordTransaction(transaction);
    }

    /**
     * Creates an error message in the output JSON.
     *
     * @param description The error description.
     * @param timestamp   The timestamp of the error.
     */
    private void createErrorOutput(final String description, final int timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", "cashWithdrawal");
        ObjectNode out2 = mapper.createObjectNode();
        out2.put("description", description);
        out.put("timestamp", timestamp);
        out2.put("timestamp", timestamp);
        out.set("output", out2);
        output.add(out);
    }
}
