package org.poo.commands;

import org.poo.entities.User;
import org.poo.entities.Transaction;
import org.poo.entities.Split;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.entities.Account.Account;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.SplitsRepository;
import org.poo.repository.TransactionRepository;
import org.poo.services.ExchangeService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public final class SplitPayment implements Command {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SplitsRepository splitsRepository;
    private final CommandInput command;
    private static final DecimalFormatSymbols SYMBOLS;
    private static final DecimalFormat DECIMAL_FORMAT;
    private final String typeOfSplit;

    // Format for printing
    static {
        SYMBOLS = new DecimalFormatSymbols();
        SYMBOLS.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.00", SYMBOLS);
    }

    public SplitPayment(final AccountRepository accountRepository,
                        final TransactionRepository transactionRepository,
                        final SplitsRepository splitsRepository,
                        final CommandInput command) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.splitsRepository = splitsRepository;
        this.command = command;
        this.typeOfSplit = command.getSplitPaymentType();
    }

    @Override
    public void execute() {

        if (typeOfSplit.equals("equal")) {
            executeEqually();
        } else {
            executeCustom();
        }
    }

    private void executeEqually() {
        List<String> accounts = command.getAccounts();
        double totalAmount = command.getAmount();
        String currency = command.getCurrency();
        double amount = totalAmount / accounts.size();
        List<String> involvedAccounts = command.getAccounts();
        int timestamp = command.getTimestamp();

        // Creează obiectul Split și îl adaugă în repository
        List<Account> accountObjects = new ArrayList<>();
        for (String accountIban : accounts) {
            Account account = accountRepository.findAccountByIban(accountIban);
            if (account == null) {
                throw new IllegalArgumentException("Account does not exist: " + accountIban);
            }
            accountObjects.add(account);
        }

        //fac o lista amountsForUsers in care pun amount de cate ori sunt conturile

        List<Double> amountsForUsers = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            amountsForUsers.add(amount);
        }

        Split split = new Split(accountObjects, command.getAmount(), currency, timestamp,
                typeOfSplit, amountsForUsers);
        splitsRepository.addSplit(split);

        // Find the account with insufficient funds
        String insufficientFundsAccount = null;

        for (String accountIban : accounts) {
            Account account = accountRepository.findAccountByIban(accountIban);

            if (account == null) {
                throw new IllegalArgumentException("Account does not exist: " + accountIban);
            }

            // Set the account with insufficient funds
            if (account.getCurrency().equals(currency)) {
                if (account.getBalance() < amount) {
                    insufficientFundsAccount = accountIban;
                }
            } else {
                ExchangeService exchangeService = ExchangeService.getInstance();
                double convertedAmount = exchangeService.convert(amount, currency,
                        account.getCurrency());
                if (account.getBalance() < convertedAmount) {
                    insufficientFundsAccount = accountIban;
                }
            }
        }

        if (insufficientFundsAccount != null) {
            // Record transactions for all the involved accounts
            for (String accountIban : accounts) {
                Account account = accountRepository.findAccountByIban(accountIban);
                if (account == null) {
                    throw new IllegalArgumentException("Account does not exist " + accountIban);
                }

                User owner = account.getOwner();

                Transaction errorTransaction = new Transaction(
                        owner.getEmail(),
                        account.getIban(),
                        null,
                        amount,
                        currency,
                        command.getTimestamp(),
                        "Split payment of " + DECIMAL_FORMAT.format(totalAmount) + " " + currency,
                        TransactionType.INSUFFICIENT_FUNDS_FOR_SPLIT,
                        null,
                        null,
                        TransactionStatus.PENDING
                );
                errorTransaction.setError("Account " + insufficientFundsAccount
                        + " has insufficient funds for a split payment.");
                errorTransaction.setStatus(TransactionStatus.PENDING);
                errorTransaction.setInvolvedAccounts(involvedAccounts);
                transactionRepository.recordTransaction(errorTransaction);
            }
            return;
        }

        for (String accountIban : accounts) {
            Account account = accountRepository.findAccountByIban(accountIban);

            if (account == null) {
                throw new IllegalArgumentException("Account does not exist " + accountIban);
            }

            User owner = account.getOwner();

            // Withdraw the money based on currency
            if (account.getCurrency().equals(currency)) {
                account.setBalance(account.getBalance() - amount);
            } else {
                ExchangeService exchangeService = ExchangeService.getInstance();
                double convertedAmount = exchangeService.convert(amount, currency,
                        account.getCurrency());
                account.setBalance(account.getBalance() - convertedAmount);
            }

            // Create transaction for every account involved
            Transaction transaction = new Transaction(
                    owner.getEmail(),
                    command.getTimestamp(),
                    "Split payment of " + DECIMAL_FORMAT.format(totalAmount) + " " + currency,
                    totalAmount,
                    currency,
                    amount,
                    accounts,
                    TransactionType.SPLIT
            );
            transaction.setStatus(TransactionStatus.PENDING);
            transactionRepository.recordTransaction(transaction);
        }
    }

    /**
     * Executes a custom split payment.
     */
    public void executeCustom() {
        List<String> accounts = command.getAccounts();
        List<Double> amountsForUsers = command.getAmountForUsers();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        List<Account> accountObjects = new ArrayList<>();
        for (String accountIban : accounts) {
            Account account = accountRepository.findAccountByIban(accountIban);
            if (account == null) {
                throw new IllegalArgumentException("Account does not exist: " + accountIban);
            }
            accountObjects.add(account);
        }

        // Add the split to the repository
        Split split = new Split(accountObjects, command.getAmount(), currency, timestamp,
                typeOfSplit, amountsForUsers);
        splitsRepository.addSplit(split);

        String insufficientFundsAccount = null;

        // Verify if there are insufficient funds for the split payment
        for (int i = 0; i < accountObjects.size(); i++) {
            Account account = accountRepository.findAccountByIban(accounts.get(i));
            if (account.getCurrency().equals(currency)) {
                if (account.getBalance() < amountsForUsers.get(i)) {
                    insufficientFundsAccount = account.getIban();
                    break;
                }
            } else {
                ExchangeService exchangeService = ExchangeService.getInstance();
                double convertedAmount = exchangeService.convert(amountsForUsers.get(i), currency,
                        account.getCurrency());
                if (account.getBalance() < convertedAmount) {
                    insufficientFundsAccount = account.getIban();
                    break;
                }
            }
        }

        // Go through all the accounts and create transactions
        for (int i = 0; i < accountObjects.size(); i++) {
            Account account = accountObjects.get(i);

            // Create transaction for every account involved
            Transaction transaction = new Transaction(
                    account.getOwner().getEmail(),
                    command.getAmountForUsers(),
                    command.getAmount(),
                    currency,
                    "Split payment of " + DECIMAL_FORMAT.format(command.getAmount())
                            + " " + currency,
                    accounts,
                    "custom",
                    TransactionType.SPLIT_CUSTOM,
                    timestamp
            );
            transaction.setStatus(TransactionStatus.PENDING);

            if (insufficientFundsAccount != null) {
                transaction.setError("Account " + insufficientFundsAccount
                        + " has insufficient funds for a split payment.");
            }
            transactionRepository.recordTransaction(transaction);
        }
    }
}
