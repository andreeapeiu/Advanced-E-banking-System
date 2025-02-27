package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Split;
import org.poo.entities.Account.Account;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.repository.AccountRepository;
import org.poo.repository.SplitsRepository;
import org.poo.repository.TransactionRepository;
import org.poo.fileio.CommandInput;
import org.poo.services.ExchangeService;

import java.util.List;

/**
 * Command for rejecting a split payment by a user.
 */
public class RejectSplitPayment implements Command {
    private final AccountRepository accountRepository;
    private final SplitsRepository splitsRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public RejectSplitPayment(final AccountRepository accountRepository,
                              final SplitsRepository splitsRepository,
                              final TransactionRepository transactionRepository,
                              final CommandInput command,
                              final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.splitsRepository = splitsRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the logic to reject a split payment.
     */
    @Override
    public void execute() {
        String email = command.getEmail();

        // Check if the user exists in any split
        int ok = 0;
        for (Split split : splitsRepository.getAllSplits()) {
            for (Account account : split.getAccounts()) {
                if (account.getOwner().getEmail().equals(email)) {
                    ok = 1;
                    break;
                }
            }
        }

        if (ok == 0) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("command", "rejectSplitPayment");
            resultNode.put("timestamp", command.getTimestamp());

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", command.getTimestamp());

            resultNode.set("output", outputNode);

            output.add(resultNode);

            return;
        }

        try {
            // Update the status of the split for the user to rejected
            Split split = splitsRepository.updateAcceptedStatus(email, false);

            // Handle the rejection logic for the split
            handleRejection(split, email);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the logic for rejecting a split.
     *
     * @param split The split object that was rejected by a user.
     * @param email The email of the user who rejected the split.
     */
    private void handleRejection(final Split split, final String email) {
        splitsRepository.removeSplit(split);
        notifyUsers(split);

        createRejectionTransaction(email, split);
    }

    /**
     * Notifies users that the split was rejected.
     *
     * @param split The split object for which the notification is sent.
     */
    private void notifyUsers(final Split split) {
        split.getAccounts().forEach(account -> {
            String ownerEmail = account.getOwner().getEmail();
        });
    }

    /**
     * Creates a transaction reflecting the rejection of the split.
     *
     * @param email The email of the user who rejected the split.
     * @param split The split object for which the transaction is created.
     */
    private void createRejectionTransaction(final String email, final Split split) {
        boolean rejectionProcessed = false;

        for (Account account : split.getAccounts()) {
            if (account.getOwner().getEmail().equalsIgnoreCase(email)) {
                double userAmount
                        = split.getAmountForUsers().get(split.getAccounts().indexOf(account));
                double finalAmount = account.getCurrency().equals(split.getCurrency())
                        ? userAmount
                        : ExchangeService.getInstance().convert(userAmount,
                        split.getCurrency(), account.getCurrency());

                // Update transactions for this split
                List<Transaction> allTransactions = transactionRepository.getAllTransactions();
                for (Transaction tr : allTransactions) {
                    if (tr.getTimestamp() == split.getTimestamp()) {
                        tr.setError("One user rejected the payment.");
                        tr.setStatus(TransactionStatus.successful);
                        rejectionProcessed = true;
                    }
                }
                break;
            }
        }
    }
}
