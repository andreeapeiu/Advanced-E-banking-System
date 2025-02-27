package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.AccountType;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.entities.User;
import org.poo.entities.Account.Account;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.services.ExchangeService;

import java.time.LocalDate;
import java.time.Period;

public class WithdrawSavings implements Command {

    private static final int AGE_LIMIT = 21;
    private static final double COMMISION_RATE = 0.02;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public WithdrawSavings(final AccountRepository accountRepository,
                           final TransactionRepository transactionRepository,
                           final CommandInput command,
                           final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
    }

    /**
     * Executes the withdrawal savings command.
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", "withdrawSavings");
        out.put("timestamp", command.getTimestamp());

        ObjectNode response = mapper.createObjectNode();

        Account account = accountRepository.getAccountByIban(command.getAccount());
        if (account == null) {
            response.put("description", "Account not found");
            out.set("output", response);
            output.add(out);
            return;
        }

        // Check if the account is a savings account
        if (!account.getAccType().equals(AccountType.savings)) {
            response.put("description", "Account is not a savings account");
            out.set("output", response);
            output.add(out);
            return;
        }

        // Verify the age of the account owner
        User owner = account.getOwner();
        String userPlan = owner.getPlan();

        int age = calculateAge(owner.getBirthDate());
        if (age < AGE_LIMIT) {
            Transaction transaction = new Transaction(
                    owner.getEmail(), account.getIban(), null,
                    0.0, account.getCurrency(), command.getTimestamp(),
                    "You don't have the minimum age required.", TransactionType.WITHDRAW_SAVINGS,
                    null, null, null);
            transactionRepository.recordTransaction(transaction);
            return;
        }

        // Find a suitable 'classic' account
        Account targetAccount = null;
        for (Account acc : accountRepository.getAllAccounts()) {
            if (acc.getOwner().equals(owner)
                    && acc.getAccType().equals(AccountType.classic)
                    && acc.getCurrency().equalsIgnoreCase(command.getCurrency())) {
                targetAccount = acc;
                break;
            }
        }

        if (targetAccount == null) {
            Transaction transaction = new Transaction(
                    owner.getEmail(), account.getIban(), null,
                    0.0, account.getCurrency(), command.getTimestamp(),
                    "You do not have a classic account.", TransactionType.WITHDRAW_SAVINGS,
                    null, null, null);
            transactionRepository.recordTransaction(transaction);
            return;
        }

        // Calculate the required amount in the savings account's currency
        double amountToWithdraw = command.getAmount();
        if (!account.getCurrency().equalsIgnoreCase(command.getCurrency())) {
            ExchangeService exchangeService = ExchangeService.getInstance();
            amountToWithdraw = exchangeService.convert(command.getAmount(),
                    command.getCurrency(),
                    account.getCurrency());
        }

        // Calculate the commission
        double commissionRate = 0.0;
        if (userPlan != null && userPlan.equalsIgnoreCase("standard")) {
            commissionRate = COMMISION_RATE;
        }

        double commission = amountToWithdraw * commissionRate;

        double totalNeeded = amountToWithdraw + commission;

        // Verify if the savings balance is sufficient
        if (account.getBalance() < totalNeeded) {
            response.put("description", "Insufficient savings balance");
            out.set("output", response);
            output.add(out);
            return;
        }

        account.setBalance(account.getBalance() - totalNeeded);
        targetAccount.setBalance(targetAccount.getBalance() + command.getAmount());

        String successDesc = "Savings withdrawal successful";
        if (commission > 0.0) {
            successDesc += String.format(", commission of %.4f %s was applied",
                    commission, account.getCurrency());
        }

        Transaction transaction = new Transaction(
                owner.getEmail(), account.getIban(), targetAccount.getIban(),
                command.getAmount(), command.getCurrency(), command.getTimestamp(),
                successDesc, TransactionType.WITHDRAW_SAVINGS,
                null, null, TransactionStatus.sent);

        transactionRepository.recordTransaction(transaction);
    }

    /**
     * Calculates the age of a user.
     *
     * @param birthDate The birthdate in the format "yyyy-MM-dd".
     * @return The age in years.
     */
    private int calculateAge(final String birthDate) {
        LocalDate birth = LocalDate.parse(birthDate);
        return Period.between(birth, LocalDate.now()).getYears();
    }
}
