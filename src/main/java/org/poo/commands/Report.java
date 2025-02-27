package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.services.TransactionService;

import java.util.HashSet;
import java.util.Set;

/**
 * Command for generating a report on account transactions.
 */
public final class Report implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public Report(final AccountRepository accountRepository,
                  final TransactionRepository transactionRepository,
                  final UserRepository userRepository,
                  final CommandInput command,
                  final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.command = command;
        this.output = output;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("command", "report");
        reportNode.put("timestamp", command.getTimestamp());

        // Obtain IBAN
        String accountIban = command.getAccount();

        // Create the output
        ObjectNode accountDetailsNode = mapper.createObjectNode();
        Account account = accountRepository.getAccountByIban(accountIban);
        if (account != null) {
            accountDetailsNode.put("IBAN", account.getIban());
            accountDetailsNode.put("balance", account.getBalance());
            accountDetailsNode.put("currency", account.getCurrency());
        } else {
            accountDetailsNode.put("description", "Account not found");
            accountDetailsNode.put("timestamp", command.getTimestamp());
            reportNode.set("output", accountDetailsNode);
            output.add(reportNode);
            return;
        }

        // Avoiding double print of the transactions with the same timestamp
        Set<Integer> processedTimestamps = new HashSet<>();

        ArrayNode transactionsArray = mapper.createArrayNode();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        User user = account.getOwner();

        for (Transaction transaction : transactionRepository.getAllTransactions()) {
            // Verify if it's the user I need
            if (!transaction.getEmail().equals(user.getEmail())) {
                continue;
            }

            // Condition for actions based on the account
            if (transaction.getType().equals(TransactionType.CREATE_CARD)
                    && !transaction.getFromAccount().equals(accountIban)) {
                continue;
            }

            // Find the transactions in the wanted range
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp) {
                // Verify if I already printed the timestamp
                if (processedTimestamps.contains(transaction.getTimestamp())) {
                    continue;
                }

                // Processed timestamps
                processedTimestamps.add(transaction.getTimestamp());

                // Add to the report
                ObjectNode transactionNode = mapper.createObjectNode();
                TransactionService.formatPrint(transaction, command, output,
                        transactionNode, userRepository);
                transactionsArray.add(transactionNode);
            }
        }

        accountDetailsNode.set("transactions", transactionsArray);
        reportNode.set("output", accountDetailsNode);

        output.add(reportNode);
        processedTimestamps.clear();
    }
}
