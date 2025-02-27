package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Account.AccountType;
import org.poo.entities.Account.SavingsAccount;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;

/**
 * Command to change the interest rate of a savings account.
 */
public final class ChangeInterestRate implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public ChangeInterestRate(final AccountRepository accountRepository,
                              final TransactionRepository transactionRepository,
                              final CommandInput command,
                              final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the change interest rate command.
     */
    @Override
    public void execute() {
        String iban = command.getAccount();
        double interestRate = command.getInterestRate();
        int timestamp = command.getTimestamp();

        Account account = accountRepository.findAccountByIban(iban);
        ObjectMapper mapper = new ObjectMapper();

        if (account == null) {
            throw new IllegalArgumentException("Account with iban: " + iban + " not found");
        }

        // Verify if it is a savings account
        if (account.getAccType().equals(AccountType.savings)) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            savingsAccount.setInterestRate(interestRate);

            Transaction transaction = new Transaction(
                    account.getOwner().getEmail(),
                    account.getIban(),
                    null,
                    savingsAccount.getInterestRate(),
                    account.getCurrency(),
                    timestamp,
                    "Interest rate of the account changed to " + savingsAccount.getInterestRate(),
                    TransactionType.CHANGE_INTEREST_RATE,
                    null,
                    null,
                    TransactionStatus.sent
            );
            transactionRepository.recordTransaction(transaction);
        } else {
            // Create the output
            ObjectNode errorResponse = mapper.createObjectNode();
            errorResponse.put("command", "changeInterestRate");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "This is not a savings account");
            outputNode.put("timestamp", timestamp);

            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", timestamp);

            output.add(errorResponse);
        }
    }

}
