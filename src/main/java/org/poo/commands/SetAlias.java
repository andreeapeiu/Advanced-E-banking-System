package org.poo.commands;

import org.poo.entities.Account.Account;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.AliasRepository;

/**
 * Command for setting an alias for an account.
 */
public final class SetAlias implements Command {
    private final AccountRepository accountRepository;
    private final AliasRepository aliasRepository;
    private final CommandInput command;

    public SetAlias(final AccountRepository accountRepository,
                    final AliasRepository aliasRepository,
                    final CommandInput command) {
        this.accountRepository = accountRepository;
        this.aliasRepository = aliasRepository;
        this.command = command;
    }

    @Override
    public void execute() {
        Account account = accountRepository.findAccountByIban(command.getAccount());
        if (account == null) {
            throw new IllegalArgumentException("Account " + command.getAccount() + " not found");
        }
        account.setAlias(command.getEmail(), command.getAccount(), command.getAlias());
    }
}
