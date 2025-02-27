package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Account.AccountType;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.entities.User;
import org.poo.entities.spendings.Spending;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.SpendingsRepository;
import org.poo.services.ExchangeService;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SpendingsReport implements Command {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SpendingsRepository spendingsRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public SpendingsReport(final AccountRepository accountRepository,
                           final CardRepository cardRepository,
                           final UserRepository userRepository,
                           final TransactionRepository transactionRepository,
                           final SpendingsRepository spendingsRepository,
                           final CommandInput command,
                           final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.spendingsRepository = spendingsRepository;
        this.command = command;
        this.output = output;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("command", "spendingsReport");
        result.put("timestamp", command.getTimestamp());

        String accountIban = command.getAccount();
        Account account = accountRepository.findAccountByIban(accountIban);

        if (account == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("description", "Account not found");
            errorNode.put("timestamp", command.getTimestamp());
            result.set("output", errorNode);
            output.add(result);
            return;
        }

        if (account.getAccType().equals(AccountType.savings)) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("error", "This kind of report is not supported for a saving account");
            result.set("output", errorNode);
            output.add(result);
            return;
        }

        User user = userRepository.findUserByEmail(account.getOwner().getEmail());
        if (user == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("description", "User not found for this account");
            result.set("output", errorNode);
            output.add(result);
            return;
        }

        ObjectNode accountDetails = mapper.createObjectNode();
        accountDetails.put("IBAN", account.getIban());
        accountDetails.put("balance", account.getBalance());
        accountDetails.put("currency", account.getCurrency());

        ArrayNode commerciantsArray = mapper.createArrayNode();
        List<Spending> spendings = spendingsRepository.getSpendingsByIban(account.getIban());

        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        ExchangeService exchangeService = ExchangeService.getInstance();

        // Calculate the total amount spent at each commerciant
        Map<String, Double> commerciantTotals = new TreeMap<>();
        for (Spending spending : spendings) {
            if (spending.getLastTimestamp() >= startTimestamp
                    && spending.getLastTimestamp() <= endTimestamp) {
                // Convert the amount to the account's currency
                double convertedAmount = exchangeService.convert(
                        spending.getTotalAmount(),
                        spending.getCurrency(),
                        account.getCurrency()
                );

                commerciantTotals.merge(
                        spending.getCommerciantName(),
                        convertedAmount,
                        Double::sum
                );
            }
        }

        // Add the commerciants to the response
        for (Map.Entry<String, Double> entry : commerciantTotals.entrySet()) {
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total", entry.getValue());
            commerciantsArray.add(commerciantNode);
        }

        accountDetails.set("commerciants", commerciantsArray);

        ArrayNode transactionsArray = mapper.createArrayNode();
        List<Transaction> transactions
                = transactionRepository.getTransactionsByAccount(accountIban);

        for (Transaction transaction : transactions) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp
                    && transaction.getEmail().equals(user.getEmail())
                    && transaction.getType() == TransactionType.PAY_ONLINE) {

                ObjectNode transactionNode = mapper.createObjectNode();
                transactionNode.put("amount", transaction.getAmount());
                transactionNode.put("commerciant", transaction.getCommerciant());
                transactionNode.put("description", transaction.getDescription());
                transactionNode.put("timestamp", transaction.getTimestamp());
                transactionsArray.add(transactionNode);
            }
        }
        accountDetails.set("transactions", transactionsArray);

        result.set("output", accountDetails);
        output.add(result);
    }

}
