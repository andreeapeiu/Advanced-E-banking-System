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

/**
 * Command to delete an account.
 */
public final class DeleteAccount implements Command {
    private static final double ACCOUNT_DELETION_THRESHOLD = 30.0;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public DeleteAccount(final AccountRepository accountRepository,
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

    /**
     * Executes the logic to delete an account.
     */
    @Override
    public void execute() {
        Account account = accountRepository.findAccountByIban(command.getAccount());
        if (account == null) {
            throw new IllegalArgumentException(
                    "Account with iban " + command.getAccount() + " not found."
            );
        }

        if (account.getBalance() - account.getMinimumBalance() > ACCOUNT_DELETION_THRESHOLD) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("command", "deleteAccount");
            outputNode.put("timestamp", command.getTimestamp());

            ObjectNode errorNode = outputNode.putObject("output");
            errorNode.put("error",
                    "Account couldn't be deleted - see org.poo.transactions for details"
            );

            errorNode.put("timestamp", command.getTimestamp());
            output.add(outputNode);

            Transaction transaction = new Transaction(
                    account.getOwner().getEmail(),
                    account.getIban(),
                    null,
                    0.0,
                    account.getCurrency(),
                    command.getTimestamp(),
                    "Account couldn't be deleted - there are funds remaining",
                    TransactionType.DELETE_ACCOUNT,
                    null,
                    null,
                    null
            );
            transactionRepository.recordTransaction(transaction);
            return;
        }

        // Find user
        User user = userRepository.findUserByEmail(command.getEmail());
        if (user == null) {
            throw new IllegalArgumentException(
                    "User with email " + command.getEmail() + " not found"
            );
        }

        user.removeAccount(account);
        accountRepository.deleteAccount(command.getAccount());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("command", "deleteAccount");
        successNode.put("timestamp", command.getTimestamp());

        ObjectNode successOutput = successNode.putObject("output");
        successOutput.put("success", "Account deleted");
        successOutput.put("timestamp", command.getTimestamp());
        output.add(successNode);
    }
}
