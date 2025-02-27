package org.poo.commands;

import org.poo.entities.Account.Account;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.entities.User;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.services.ExchangeService;
import org.poo.fileio.CommandInput;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command for upgrading a user's plan.
 */
public class UpgradePlan implements Command {
    private static final int PRAG = 5;
    private static final int SILVER_TO_GOLD = 250;
    private static final int STANDARD_TO_SILVER = 100;
    private static final int STANDARD_TO_GOLD = 350;
    private static final int GOLD = 3;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ExchangeService exchangeService;
    private final ArrayNode output;
    private final CommandInput command;

    public UpgradePlan(final TransactionRepository transactionRepository,
                       final AccountRepository accountRepository,
                       final CommandInput command,
                       final UserRepository userRepository,
                       final ArrayNode output,
                       final ExchangeService exchangeService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.output = output;
        this.command = command;
        this.exchangeService = exchangeService;
    }

    /**
     * Executes the upgrade plan command.
     */
    @Override
    public void execute() {
        String newPlanType = command.getNewPlanType();
        String iban = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = accountRepository.getAccountByIban(iban);
        if (account == null) {
            createErrorOutput("Account not found", timestamp);
            return;
        }

        User user = account.getOwner();
        String currentPlan = user.getPlan();

        // Check if the user already has the requested plan
        if (currentPlan.equalsIgnoreCase(newPlanType)) {
            return;
        }

        // Validate if the upgrade is valid (not a downgrade)
        if (!isUpgradeValid(currentPlan, newPlanType)) {
            return;
        }

        // Determine if the upgrade is automatic or manual
        boolean isAutomatic = false;
        if (currentPlan.equalsIgnoreCase("silver") && newPlanType.equalsIgnoreCase("gold")) {
            // Check if the user has 5 eligible payments
            if (user.getEligiblePaymentsForGold() >= PRAG) {
                isAutomatic = true;
            }
        }

        double fee = 0.0;
        if (!isAutomatic) {
            // Calculate the upgrade fee
            fee = calculateUpgradeFee(currentPlan, newPlanType);

            // Check if the user has sufficient funds for the fee
            if (account.getCurrency().equalsIgnoreCase("RON")) {
                if (account.getBalance() < fee) {
                    recordTransaction(user, account, fee, "Insufficient funds",
                            TransactionType.UPGRADE_PLAN_NO_FUNDS, timestamp, newPlanType,
                            TransactionStatus.failed);
                    return;
                }
            } else {
                // Convert the fee to the account's currency
                double feeInAccountCurrency = exchangeService.convert(fee, "RON",
                        account.getCurrency());
                if (account.getBalance() < feeInAccountCurrency) {
                    recordTransaction(user, account, fee, "Insufficient funds",
                            TransactionType.UPGRADE_PLAN_NO_FUNDS, timestamp, newPlanType,
                            TransactionStatus.failed);
                    return;
                }
                fee = feeInAccountCurrency;
            }

            account.setBalance(account.getBalance() - fee);
        }

        upgradeUserPlan(user, newPlanType, timestamp, isAutomatic);

        // Record the upgrade transaction
        recordTransaction(user, account, 0.0, isAutomatic
                ? "Automatic upgrade to Gold" : "Upgrade plan", TransactionType.UPGRADE_PLAN,
                timestamp, newPlanType, TransactionStatus.sent);
        isAutomatic = false;
    }

    /**
     * Updates the user's plan programmatically without a fee.
     *
     * @param user         The user upgrading their plan.
     * @param newPlanType  The new plan type.
     * @param timestamp    The timestamp of the upgrade.
     * @param isAutomatic  Indicates if the upgrade is automatic or manual.
     */
    public void upgradeUserPlan(final User user, final String newPlanType, final int timestamp,
                                final boolean isAutomatic) {
        String currentPlan = user.getPlan();

        // User already has the desired plan
        if (currentPlan.equalsIgnoreCase(newPlanType)) {
            return;
        }

        if (!isUpgradeValid(currentPlan, newPlanType)) {
            return;
        }

        user.setPlan(newPlanType.toLowerCase());
    }

    /**
     * Validates if upgrading from the current plan to the new plan is valid (not a downgrade).
     *
     * @param currentPlan The user's current plan.
     * @param newPlan     The new plan to upgrade to.
     * @return True if the upgrade is valid, false otherwise.
     */
    private boolean isUpgradeValid(final String currentPlan, final String newPlan) {
        int currentRank = getPlanRank(currentPlan);
        int newRank = getPlanRank(newPlan);
        return newRank > currentRank; // Only rank increases are valid
    }

    /**
     * Assigns a numeric rank to each plan for comparison purposes.
     *
     * @param plan The type of plan.
     * @return The numeric rank of the plan.
     */
    private int getPlanRank(final String plan) {
        switch (plan.toLowerCase()) {
            case "student":
            case "standard":
                return 1;
            case "silver":
                return 2;
            case "gold":
                return GOLD;
            default:
                return 0;
        }
    }

    /**
     * Calculates the fee for upgrading based on the specified rules.
     *
     * @param currentPlan The user's current plan.
     * @param newPlan     The new plan to upgrade to.
     * @return The fee in RON.
     */
    private double calculateUpgradeFee(final String currentPlan, final String newPlan) {
        String c = currentPlan.toLowerCase();
        String n = newPlan.toLowerCase();

        if ((c.equals("standard") || c.equals("student")) && n.equals("silver")) {
            return STANDARD_TO_SILVER;
        } else if (c.equals("silver") && n.equals("gold")) {
            return SILVER_TO_GOLD;
        } else if ((c.equals("standard") || c.equals("student")) && n.equals("gold")) {
            return STANDARD_TO_GOLD;
        }
        return 0.0;
    }

    /**
     * Records a transaction with the specified details.
     *
     * @param user         The user associated with the transaction.
     * @param account      The account involved in the transaction.
     * @param amount       The transaction amount.
     * @param description  The transaction description.
     * @param type         The type of the transaction.
     * @param timestamp    The transaction timestamp.
     * @param newPlanType  The new plan type (if applicable).
     * @param status       The transaction status.
     */
    private void recordTransaction(final User user, final Account account, final double amount,
                                   final String description, final TransactionType type,
                                   final int timestamp, final String newPlanType,
                                   final TransactionStatus status) {
        Transaction transaction = new Transaction(
                user.getEmail(),
                account.getIban(),
                account.getIban(),
                amount,
                account.getCurrency(),
                timestamp,
                description,
                type,
                null,
                newPlanType,
                status
        );
        transactionRepository.recordTransaction(transaction);
    }

    /**
     * Creates an error message and adds it to the output.
     *
     * @param description The error description.
     * @param timestamp   The timestamp of the error.
     */
    private void createErrorOutput(final String description, final int timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", "upgradePlan");
        ObjectNode out2 = mapper.createObjectNode();
        out2.put("description", description);
        out.put("timestamp", timestamp);
        out2.put("timestamp", timestamp);
        out.set("output", out2);
        output.add(out);
    }
}
