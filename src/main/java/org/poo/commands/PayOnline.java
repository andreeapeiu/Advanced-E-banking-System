package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.User;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.entities.TransactionStatus;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.CardType;
import org.poo.entities.Card.CardStatus;
import org.poo.entities.Card.Card;
import org.poo.entities.spendings.Spending;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.SpendingsRepository;
import org.poo.repository.UserRepository;
import org.poo.services.cashback.CashbackCalculator;
import org.poo.services.CommerciantsService;
import org.poo.services.ExchangeService;

/**
 * Command for processing online payments with cards and applying cashback.
 */
public final class PayOnline implements Command {
    private static final double STANDARD_COMMISSION_PERCENT = 0.2;
    private static final double SILVER_COMMISSION_PERCENT = 0.1;
    private static final double COMMISSION_DIVISOR = 100.0;
    private static final double CASHBACK_THRESHOLD = 500.0;

    private final CommerciantsService commerciantsService;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final SpendingsRepository spendingsRepository;
    private final UserRepository userRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public PayOnline(final AccountRepository accountRepository,
                     final CardRepository cardRepository,
                     final TransactionRepository transactionRepository,
                     final SpendingsRepository spendingsRepository,
                     final CommerciantsService commerciantsService,
                     final UserRepository userRepository,
                     final CommandInput command,
                     final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.spendingsRepository = spendingsRepository;

        this.commerciantsService = commerciantsService;
        this.command = command;
        this.output = output;
    }

    @Override
    public void execute() {
        String number = command.getCardNumber();
        double amount = command.getAmount();

        Card card = cardRepository.getCardByNumber(number);
        if (card == null) {
            createErrorOutput("Card not found", command.getTimestamp());
            return;
        }

        Account account = accountRepository.getAccountByIban(card.getAccountIban());
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist.");
        }

        if (card.getStatus().equals(CardStatus.frozen)) {
            createErrorOutput("The card is frozen", command.getTimestamp());
            return;
        }

        double convertedAmount = amount;
        if (!account.getCurrency().equals(command.getCurrency())) {
            ExchangeService exchangeService = ExchangeService.getInstance();
            convertedAmount = exchangeService.convert(amount, command.getCurrency(),
                    account.getCurrency());
        }

        if (account.getBalance() - account.getBlockAmount() < convertedAmount) {
            return;
        }

        if (convertedAmount == 0) {
            return;
        }

        account.setBalance(account.getBalance() - convertedAmount);

        User user = userRepository.findUserByEmail(account.getOwner().getEmail());
        user.setTotalSpent(user.getTotalSpent() + convertedAmount);

        ExchangeService exchangeService = ExchangeService.getInstance();
        double commission = 0.0;
        double suminron = exchangeService.convert(convertedAmount, account.getCurrency(), "RON");

        if (account.getOwner().getPlan().equals("standard")) {
            commission = (STANDARD_COMMISSION_PERCENT / COMMISSION_DIVISOR) * convertedAmount;
        } else if (account.getOwner().getPlan().equals("silver")) {
            if (suminron >= CASHBACK_THRESHOLD) {
                commission = (SILVER_COMMISSION_PERCENT / COMMISSION_DIVISOR) * convertedAmount;
            }
        }

        account.setBalance(account.getBalance() - commission);

        Transaction transaction = new Transaction(
                command.getEmail(),
                account.getIban(),
                account.getIban(),
                convertedAmount,
                command.getCurrency(),
                command.getTimestamp(),
                "Card payment",
                TransactionType.PAY_ONLINE,
                number,
                command.getCommerciant(),
                TransactionStatus.sent
        );
        transaction.setSpending(true);
        transactionRepository.recordTransaction(transaction);

        Spending spending = new Spending(
                account.getOwner().getEmail(),
                command.getCommerciant(),
                amount,
                command.getTimestamp(),
                account.getIban(),
                command.getCurrency()
        );
        spendingsRepository.recordSpending(spending);

        CashbackCalculator cashbackCalculator = new CashbackCalculator(
                commerciantsService,
                ExchangeService.getInstance(),
                accountRepository
        );

        Commerciant commerciant = getCommerciantFromInput(command.getCommerciant());
        if (commerciant == null) {
            createErrorOutput("Commerciant not found in repository", command.getTimestamp());
            return;
        }

        double cashback = cashbackCalculator.calculateCashback(
                account.getIban(),
                transaction,
                commerciant.getId(),
                accountRepository,
                commerciantsService,
                exchangeService
        );

        if (cashback > 0) {
            account.deposit(cashback);
        }

        if (card.getCardType().equals(CardType.ONE_TIME)) {
            Transaction transaction1 = new Transaction(
                    account.getEmail(),
                    account.getIban(),
                    account.getIban(),
                    0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "The card has been destroyed",
                    TransactionType.DELETE_CARD,
                    card.getCardNumber(),
                    null,
                    null
            );
            transactionRepository.recordTransaction(transaction1);

            card.setCardNumber(card);

            Transaction transaction2 = new Transaction(
                    account.getEmail(),
                    account.getIban(),
                    account.getIban(),
                    0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "New card created",
                    TransactionType.CREATE_CARD,
                    card.getCardNumber(),
                    null,
                    null
            );
            transactionRepository.recordTransaction(transaction2);
        }
    }

    private Commerciant getCommerciantFromInput(final String commerciantName) {
        return commerciantsService.getCommerciantByName(commerciantName);
    }

    private void createErrorOutput(final String description, final int timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", "payOnline");
        ObjectNode out2 = mapper.createObjectNode();
        out2.put("description", description);
        out.put("timestamp", timestamp);
        out2.put("timestamp", timestamp);
        out.set("output", out2);
        output.add(out);
    }
}
