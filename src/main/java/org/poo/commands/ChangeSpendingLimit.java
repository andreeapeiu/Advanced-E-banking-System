package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.BusinessAccount;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;

/**
 * Command to change the spending limit for a business account.
 */
public final class ChangeSpendingLimit implements Command {
    private final AccountRepository accountRepository;
    private final CommandInput command;
    private final ArrayNode output;

    /**
     * Constructor for ChangeSpendingLimit.
     *
     * @param accountRepository Repository for managing accounts.
     * @param command           Command input containing account and limit details.
     * @param output            JSON array node to store the output messages.
     */
    public ChangeSpendingLimit(final AccountRepository accountRepository,
                               final CommandInput command,
                               final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the command to change the spending limit.
     */
    @Override
    public void execute() {
        final String accountIban = command.getAccount();
        final String userEmail = command.getEmail();
        final double newLimit = command.getAmount();

        final BusinessAccount businessAccount =
                (BusinessAccount) accountRepository.findAccountByIban(accountIban);

        if (businessAccount == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        if (!"business".equalsIgnoreCase(businessAccount.getAccountType())) {
            throw new IllegalArgumentException("Account is not of type business.");
        }

        // Check if the requesting user is the owner of the account
        if (!businessAccount.getOwnerEmail().equalsIgnoreCase(userEmail)) {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode out = mapper.createObjectNode();
            out.put("command", "changeSpendingLimit");
            final ObjectNode out2 = mapper.createObjectNode();
            out2.put("description", "You must be owner in order to change spending limit.");
            out.put("timestamp", command.getTimestamp());
            out2.put("timestamp", command.getTimestamp());
            out.set("output", out2);
            output.add(out);
            return;
        }

        // Update the spending limit for the target user
        final String targetEmail = command.getEmail(); // Target user's email.
        businessAccount.updateLimit(targetEmail, "spend", newLimit);
    }
}
