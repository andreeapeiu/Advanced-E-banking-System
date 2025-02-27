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
 * Command to add interest to a savings account.
 */
public final class AddInterest implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public AddInterest(final AccountRepository accountRepository,
                       final TransactionRepository transactionRepository,
                       final CommandInput command,
                       final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the add interest command.
     */
    @Override
    public void execute() {
        Account account = accountRepository.findAccountByIban(command.getAccount());
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + command.getAccount());
        }

        if (!account.getAccType().equals(AccountType.savings)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode errorResponse = mapper.createObjectNode();
            errorResponse.put("command", "addInterest");
            errorResponse.put("timestamp", command.getTimestamp());
            ObjectNode outputNode = errorResponse.putObject("output");
            outputNode.put("description", "This is not a savings account");
            outputNode.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        SavingsAccount savingsAccount = (SavingsAccount) account;
        double interestRate = savingsAccount.getInterestRate();

        // Calculate the interest and add it to the savings account
        double interest = account.getBalance() * interestRate;
        account.setBalance(account.getBalance() + interest);

        Transaction transaction = new Transaction(
                account.getOwner().getEmail(),
                account.getIban(),
                null,
                interest,
                account.getCurrency(),
                command.getTimestamp(),
                "Interest rate income",
                TransactionType.ADD_INTEREST,
                null,
                null,
                TransactionStatus.sent
        );
        transactionRepository.recordTransaction(transaction);
    }
}
