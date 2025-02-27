package org.poo.commands;

import org.poo.entities.Account.BusinessAccount;
import org.poo.entities.User;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionType;
import org.poo.entities.Account.Account;
import org.poo.entities.Account.ClassicAccount;
import org.poo.entities.Account.SavingsAccount;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.AliasRepository;
import org.poo.utils.Utils;

public final class AddAccount implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final AliasRepository aliasRepository;
    private final CommandInput command;

    public AddAccount(final AccountRepository accountRepository,
                      final TransactionRepository transactionRepository,
                      final UserRepository userRepository,
                      final CardRepository cardRepository,
                      final AliasRepository aliasRepository,
                      final CommandInput command) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.aliasRepository = aliasRepository;
        this.command = command;
    }

    @Override
    public void execute() {
        User user = userRepository.findUserByEmail(command.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Account account;
        if ("classic".equals(command.getAccountType())) {
            account = new ClassicAccount(
                    Utils.generateIBAN(),
                    command.getCurrency(),
                    0,
                    user,
                    cardRepository,
                    aliasRepository
            );
        } else if ("savings".equals(command.getAccountType())) {
            account = new SavingsAccount(
                    Utils.generateIBAN(),
                    command.getCurrency(),
                    command.getInterestRate(),
                    user,
                    cardRepository,
                    aliasRepository
            );
        } else if ("business".equals(command.getAccountType())) {
            account = new BusinessAccount(
                    Utils.generateIBAN(),
                    command.getCurrency(),
                    user,
                    cardRepository,
                    aliasRepository
            );
        } else {
            throw new IllegalArgumentException("Account type invalid " + command.getAccountType());
        }

        user.addAccount(account);
        accountRepository.addAccount(account);

        Transaction transaction = new Transaction(
                command.getEmail(),
                account.getIban(),
                account.getIban(),
                0.0,
                account.getCurrency(),
                command.getTimestamp(),
                "New account created",
                TransactionType.ADD_ACCOUNT,
                null,
                null,
                null
        );
        transactionRepository.recordTransaction(transaction);
    }
}
