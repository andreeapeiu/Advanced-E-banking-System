package org.poo.commands;

import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.entities.Account.Account;

/**
 * Command to set the minimum balance for an account.
 */
public final class SetMinimumBalance implements Command {
    private final AccountRepository accountRepository;
    private final CommandInput command;

    public SetMinimumBalance(final AccountRepository accountRepository,
                             final CommandInput command) {
        this.accountRepository = accountRepository;
        this.command = command;
    }

    @Override
    public void execute() {
        Account account = accountRepository.findAccountByIban(command.getAccount());
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + command.getAccount());
        }

        account.setMinimumBalance(command.getAmount());
        account.setHasMinimumBalance(true);
    }
}
