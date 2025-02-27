package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Split;
import org.poo.entities.Account.Account;
import org.poo.repository.AccountRepository;
import org.poo.repository.SplitsRepository;
import org.poo.repository.TransactionRepository;
import org.poo.fileio.CommandInput;
import org.poo.services.ExchangeService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * Command class to handle the acceptance of a split payment by a user.
 */
public class AcceptSplitPayment implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SplitsRepository splitsRepository;
    private final CommandInput command;
    private final ArrayNode output;

    private static final DecimalFormatSymbols SYMBOLS;
    private static final DecimalFormat DECIMAL_FORMAT;

    // Static block to initialize the decimal format for consistent formatting of monetary values.
    static {
        SYMBOLS = new DecimalFormatSymbols();
        SYMBOLS.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.00", SYMBOLS);
    }

    /**
     * Constructor to initialize repositories, command input, and output nodes.
     *
     * @param accountRepository     Repository for managing accounts.
     * @param transactionRepository Repository for managing transactions.
     * @param splitsRepository      Repository for managing splits.
     * @param command               Command input containing user data.
     * @param output                Output node to store the result of the command.
     */
    public AcceptSplitPayment(final AccountRepository accountRepository,
                              final TransactionRepository transactionRepository,
                              final SplitsRepository splitsRepository,
                              final CommandInput command,
                              final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.splitsRepository = splitsRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the logic to accept a split payment.
     * - Validates if the user is part of any split.
     * - Updates the user's acceptance status.
     * - Withdraws funds from the user's account.
     * - Finalizes the split if all users accept.
     */
    @Override
    public void execute() {
        String email = command.getEmail();

        // Check if the user is part of any split.
        int ok = 0;
        for (Split split : splitsRepository.getAllSplits()) {
            for (Account account : split.getAccounts()) {
                if (account.getOwner().getEmail().equals(email)) {
                    ok = 1;
                    break;
                }
            }
        }

        // If the user is not part of any split, add a response to the output and return.
        if (ok == 0) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("command", "acceptSplitPayment");
            resultNode.put("timestamp", command.getTimestamp());

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", command.getTimestamp());

            resultNode.set("output", outputNode);

            output.add(resultNode);

            return;
        }

        try {
            // Update the acceptance status for the user.
            Split split = splitsRepository.updateAcceptedStatus(email, true);

            withdrawFunds(email, split);

            // Finalize the split if all users have accepted it.
            if (split.allAccepted()) {
                finalizeSplit(split);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Withdraws funds from the account of the user who accepted the split.
     *
     * @param email Email of the user accepting the split.
     * @param split Split object associated with the payment.
     */
    private void withdrawFunds(final String email, final Split split) {
        List<Account> accounts = split.getAccounts();
        List<Double> amounts = split.getAmountForUsers();
        String currency = split.getCurrency();

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getOwner().getEmail().equalsIgnoreCase(email)) {
                double amount = amounts.get(i);
                double finalAmount = account.getCurrency().equals(currency)
                        ? amount
                        : ExchangeService.getInstance().convert(amount, currency,
                        account.getCurrency());

                if (account.getBalance() < finalAmount) {
                    return;
                }

                // Block the required amount for the split payment.
                account.setBlockAmount(account.getBlockAmount() + finalAmount);
              //  return;
            }
        }
    }

    /**
     * Finalizes the split payment by removing the split and logging the transaction.
     *
     * @param split Split object that has been fully accepted.
     */
    private void finalizeSplit(final Split split) {
        splitsRepository.removeSplit(split);
    }
}
