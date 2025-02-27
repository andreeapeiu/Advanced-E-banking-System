package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.BusinessAccount;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;

/**
 * Command to change the deposit limit for a business account.
 */
public final class ChangeDepositLimit implements Command {
    private final AccountRepository accountRepository;
    private final CommandInput command;
    private final ArrayNode output;

    /**
     * Constructor for ChangeDepositLimit.
     *
     * @param accountRepository Repository for managing accounts.
     * @param command           Command input containing the account and limit details.
     * @param output            JSON array node to store the output messages.
     */
    public ChangeDepositLimit(final AccountRepository accountRepository,
                              final CommandInput command,
                              final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the command to change the deposit limit.
     */
    @Override
    public void execute() {
        final String accountIban = command.getAccount();
        final String userEmail = command.getEmail();
        final double newLimit = command.getAmount();

        final BusinessAccount businessAccount =
                (BusinessAccount) accountRepository.findAccountByIban(accountIban);

        if (businessAccount == null) {
            generateErrorOutput("Account not found.", command.getTimestamp());
            return;
        }

        if (!"business".equalsIgnoreCase(businessAccount.getAccountType())) {
            generateErrorOutput("Account is not of type business.", command.getTimestamp());
            return;
        }

        // Check if the requesting user is the owner of the account
        if (!businessAccount.getOwnerEmail().equalsIgnoreCase(userEmail)) {
            generateErrorOutput("You must be owner in order "
                    + "to change deposit limit.", command.getTimestamp());
            return;
        }

        // Update the deposit limit for the target user
        final String targetEmail = command.getEmail(); // Target user's email.
        businessAccount.updateLimit(targetEmail, "deposit", newLimit);
    }

    /**
     * Generates an error message in the output JSON.
     *
     * @param errorMessage The error message to include.
     * @param timestamp    The timestamp of the error.
     */
    private void generateErrorOutput(final String errorMessage, final int timestamp) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode out = mapper.createObjectNode();
        out.put("command", "changeDepositLimit");
        out.put("timestamp", timestamp);

        final ObjectNode errorOutput = mapper.createObjectNode();
        errorOutput.put("description", errorMessage);
        errorOutput.put("timestamp", timestamp);

        out.set("output", errorOutput);
        output.add(out);
    }
}
