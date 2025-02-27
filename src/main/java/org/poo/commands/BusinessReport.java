package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.BusinessAccount;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.spendings.Spending;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.SpendingsRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.services.ExchangeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Command class to generate business reports.
 */
public final class BusinessReport implements Command {
    private static final double DEFAULT_LIMIT_RON = 500.0;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SpendingsRepository spendingsRepository;
    private final CommandInput command;
    private final ArrayNode output;

    /**
     * Constructor to initialize the repositories and command input.
     *
     * @param accountRepository Repository for managing accounts.
     * @param transactionRepository Repository for managing transactions.
     * @param userRepository Repository for managing users.
     * @param spendingsRepository Repository for managing spendings.
     * @param command Command input for report generation.
     * @param output Output node to store the result.
     */
    public BusinessReport(final AccountRepository accountRepository,
                          final TransactionRepository transactionRepository,
                          final UserRepository userRepository,
                          final SpendingsRepository spendingsRepository,
                          final CommandInput command,
                          final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.spendingsRepository = spendingsRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the command to generate the business report.
     */
    @Override
    public void execute() {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode result = mapper.createObjectNode();
        result.put("command", "businessReport");
        result.put("timestamp", command.getTimestamp());

        // Retrieve the business account by IBAN
        final String accountIban = command.getAccount();
        final BusinessAccount businessAccount =
                (BusinessAccount) accountRepository.findAccountByIban(accountIban);

        if (businessAccount == null) {
            result.put("error", "Account not found");
            output.add(result);
            return;
        }

        if (!"business".equals(businessAccount.getAccountType())) {
            result.put("error", "Account is not of type business");
            output.add(result);
            return;
        }

        final String reportType = command.getType();
        final int startTimestamp = command.getStartTimestamp();
        final int endTimestamp = command.getEndTimestamp();

        if ("transaction".equals(reportType)) {
            generateTransactionReport(
                    mapper, result, businessAccount, startTimestamp, endTimestamp
            );
        } else if ("commerciant".equals(reportType)) {
            generateCommerciantReport(
                    mapper, result, businessAccount, startTimestamp, endTimestamp
            );
        } else {
            result.put("error", "Invalid report type");
        }

        output.add(result);
    }

    private void generateTransactionReport(final ObjectMapper mapper,
                                           final ObjectNode result,
                                           final BusinessAccount businessAccount,
                                           final int startTimestamp,
                                           final int endTimestamp) {
        final ObjectNode outputData = mapper.createObjectNode();
        outputData.put("IBAN", businessAccount.getIban());
        outputData.put("balance", businessAccount.getBalance());
        outputData.put("currency", businessAccount.getCurrency());

        final ExchangeService exchangeService = ExchangeService.getInstance();

        // Convert default limits to the account currency
        final double defaultSpendingLimit = exchangeService.convert(
                DEFAULT_LIMIT_RON, "RON", businessAccount.getCurrency()
        );
        final double defaultDepositLimit = exchangeService.convert(
                DEFAULT_LIMIT_RON, "RON", businessAccount.getCurrency()
        );

        // Add limits to the output.
        outputData.put("spending limit", businessAccount.getSpendingLimits()
                .getOrDefault(businessAccount.getOwnerEmail(), defaultSpendingLimit));
        outputData.put("deposit limit", businessAccount.getDepositLimits()
                .getOrDefault(businessAccount.getOwnerEmail(), defaultDepositLimit));
        outputData.put("statistics type", "transaction");

        final ArrayNode managersArray = mapper.createArrayNode();
        final ArrayNode employeesArray = mapper.createArrayNode();
        final Map<String, Double> spentByUser = new HashMap<>();
        final Map<String, Double> depositedByUser = new HashMap<>();

        // Process spendings
        final List<Spending> spendings
                = spendingsRepository.getSpendingsByIban(businessAccount.getIban());
        for (final Spending spending : spendings) {
            if (spending.getLastTimestamp() >= startTimestamp
                    && spending.getLastTimestamp() <= endTimestamp) {
                final double convertedAmount = exchangeService.convert(
                        spending.getTotalAmount(), spending.getCurrency(),
                        businessAccount.getCurrency()
                );
                final String email = spending.getEmail();
                spentByUser.put(email, spentByUser.getOrDefault(email, 0.0) + convertedAmount);
            }
        }

        // Process transactions.
        final List<Transaction> transactions =
                transactionRepository.getTransactionsByAccount(businessAccount.getIban());
        for (final Transaction transaction : transactions) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp) {
                final String email = transaction.getEmail();
                if (transaction.isSpending()) {
                    spentByUser.put(email, spentByUser.getOrDefault(email, 0.0)
                            + transaction.getAmount());
                } else if (transaction.isDeposit()) {
                    depositedByUser.put(email, depositedByUser.getOrDefault(email, 0.0)
                            + transaction.getAmount());
                }
            }
        }

        double totalSpent = 0.0;
        double totalDeposited = 0.0;

        for (final Map.Entry<String, String> associate
                : businessAccount.getAssociates().entrySet()) {
            final String email = associate.getKey();
            final String role = associate.getValue();
            final double spent = spentByUser.getOrDefault(email, 0.0);
            final double deposited = depositedByUser.getOrDefault(email, 0.0);

            totalSpent += spent;
            totalDeposited += deposited;

            final ObjectNode userNode = mapper.createObjectNode();
            final User user = userRepository.findUserByEmail(email);
            final String formattedName = user.getLastName() + " " + user.getFirstName();

            userNode.put("username", formattedName);
            userNode.put("spent", spent);
            userNode.put("deposited", deposited);

            if ("manager".equals(role)) {
                managersArray.add(userNode);
            } else if ("employee".equals(role)) {
                employeesArray.add(userNode);
            }
        }

        // Add grouped data to the output.
        outputData.set("managers", managersArray);
        outputData.set("employees", employeesArray);
        outputData.put("total spent", totalSpent);
        outputData.put("total deposited", totalDeposited);

        result.set("output", outputData);
    }

    private void generateCommerciantReport(final ObjectMapper mapper,
                                           final ObjectNode result,
                                           final BusinessAccount businessAccount,
                                           final int startTimestamp,
                                           final int endTimestamp) {
        final ObjectNode outputData = mapper.createObjectNode();
        outputData.put("IBAN", businessAccount.getIban());

        final Map<String, Double> amountsByCommerciant = new TreeMap<>();
        final Map<String, List<String>> usersByCommerciant = new HashMap<>();

        final List<Transaction> transactions =
                transactionRepository.getTransactionsByAccount(businessAccount.getIban());
        for (final Transaction transaction : transactions) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp
                    && transaction.isSpending()) {
                final String commerciant = transaction.getCommerciant();
                final double amount = transaction.getAmount();
                final String email = transaction.getEmail();

                amountsByCommerciant.put(
                        commerciant, amountsByCommerciant.getOrDefault(commerciant, 0.0) + amount
                );

                usersByCommerciant.computeIfAbsent(commerciant, k -> new ArrayList<>());
                if (!usersByCommerciant.get(commerciant).contains(email)) {
                    usersByCommerciant.get(commerciant).add(email);
                }
            }
        }

        final ArrayNode commerciantsArray = mapper.createArrayNode();
        for (final Map.Entry<String, Double> entry : amountsByCommerciant.entrySet()) {
            final String commerciant = entry.getKey();
            final double totalSpent = entry.getValue();

            final ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", commerciant);
            commerciantNode.put("total spent", totalSpent);

            final List<String> users = usersByCommerciant.get(commerciant);
            users.sort(String::compareTo);

            final ArrayNode usersArray = mapper.createArrayNode();
            for (final String user : users) {
                usersArray.add(user);
            }

            commerciantNode.set("users", usersArray);
            commerciantsArray.add(commerciantNode);
        }

        outputData.set("commerciants", commerciantsArray);
        result.set("output", outputData);
    }
}
