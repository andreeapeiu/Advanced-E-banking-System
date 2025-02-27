package org.poo.commands;

import org.poo.entities.Account.AccountType;
import org.poo.entities.Account.BusinessAccount;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.entities.Account.Account;
import org.poo.repository.TransactionRepository;

/**
 * Command class to add funds to an account.
 */
public final class AddFunds implements Command {
    private final AccountRepository accountRepository;
    private final CommandInput command;
    private final TransactionRepository transactionRepository;

    /**
     * Constructor to initialize repositories and command input.
     *
     * @param accountRepository     Repository for managing accounts.
     * @param command               Command input containing account and transaction details.
     * @param transactionRepository Repository for managing transactions.
     */
    public AddFunds(final AccountRepository accountRepository,
                    final CommandInput command,
                    final TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
    }

    /**
     * Executes the add funds command.
     * - Finds the target account by IBAN.
     * - Validates permissions and deposit limits (for business accounts).
     * - Updates the account balance.
     * - Records the transaction in the repository.
     */
    @Override
    public void execute() {
        // Retrieve the account based on IBAN.
        Account account = accountRepository.findAccountByIban(command.getAccount());
        if (account == null) {
            throw new IllegalArgumentException(
                    "Account with IBAN " + command.getAccount() + " not found."
            );
        }

        if (account.getAccType().equals(AccountType.business)) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            String userEmail = command.getEmail();

            // Check if the user is the owner or manager
            if (!(businessAccount.getOwner().getEmail().equals(userEmail)
                    || "manager".equals(businessAccount.getAssociates().get(userEmail)))) {
                // Validate deposit limits for other users.
                if (businessAccount.getDepositLimit() < command.getAmount()) {
                    return;
                }
            }

        }

        account.setBalance(account.getBalance() + command.getAmount());

        // Create and record the transaction.
        Transaction transaction = new Transaction(
                command.getEmail(),
                account.getIban(),
                account.getIban(),
                command.getAmount(),
                command.getCurrency(),
                command.getTimestamp(),
                "Add funds",
                TransactionType.DEPOSIT,
                null,
                command.getCommerciant(),
                TransactionStatus.sent
        );
        // Mark the transaction as a deposit.
        transaction.setDeposit(true);
        transactionRepository.recordTransaction(transaction);
    }
}
