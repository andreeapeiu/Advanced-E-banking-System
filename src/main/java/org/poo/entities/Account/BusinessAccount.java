package org.poo.entities.Account;

import org.poo.entities.User;
import org.poo.repository.AliasRepository;
import org.poo.repository.CardRepository;
import org.poo.services.ExchangeService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a business account with specific properties and methods.
 */
public class BusinessAccount extends Account {
    private static final double FIVE_HUNDRED_RON = 500.0;
    private double interestRate;
    private String ownerEmail;
    private final Map<String, String> associates;
    private double minBalance;
    private final Map<String, Double> spendingLimits;
    private final Map<String, Double> depositLimits;
    private double depositLimit;

    /**
     * Constructs a BusinessAccount with the specified parameters.
     *
     * @param iban           the IBAN of the account
     * @param currency       the currency of the account
     * @param owner          the owner of the account
     * @param cardRepository the card repository
     * @param aliasRepository the alias repository
     */
    public BusinessAccount(final String iban, final String currency, final User owner,
                           final CardRepository cardRepository,
                           final AliasRepository aliasRepository) {
        super(iban, currency, owner, cardRepository, aliasRepository);
        this.ownerEmail = owner.getEmail();
        this.associates = new HashMap<>();
        this.spendingLimits = new HashMap<>();
        this.depositLimits = new HashMap<>();
        this.depositLimit = ExchangeService.getInstance().convert(FIVE_HUNDRED_RON,
                "RON", currency);
        setAccType(AccountType.business);
    }

    /**
     * Gets the minimum balance of the account.
     *
     * @return the minimum balance
     */
    public double getMinBalance() {
        return minBalance;
    }

    /**
     * Sets the minimum balance for the account.
     *
     * @param minBalance the minimum balance to set
     */
    public void setMinBalance(final double minBalance) {
        this.minBalance = minBalance;
    }

    /**
     * Gets the owner's email address.
     *
     * @return the owner's email address
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Sets the owner's email address.
     *
     * @param ownerEmail the owner's email address to set
     */
    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * Gets the account type.
     *
     * @return the account type as a string
     */
    @Override
    public String getAccountType() {
        return "business";
    }

    /**
     * Gets the interest rate for the account.
     *
     * @return the interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Sets the interest rate for the account.
     *
     * @param interestRate the interest rate to set
     */
    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Gets the spending limits for associates.
     *
     * @return a map of spending limits by email
     */
    public Map<String, Double> getSpendingLimits() {
        return this.spendingLimits;
    }

    /**
     * Gets the deposit limits for associates.
     *
     * @return a map of deposit limits by email
     */
    public Map<String, Double> getDepositLimits() {
        return this.depositLimits;
    }

    /**
     * Gets the associates of the account.
     *
     * @return a map of associates by email and their roles
     */
    public Map<String, String> getAssociates() {
        return this.associates;
    }

    /**
     * Gets the deposit limit for the account.
     *
     * @return the deposit limit
     */
    public double getDepositLimit() {
        return depositLimit;
    }

    /**
     * Sets the deposit limit for the account.
     *
     * @param depositLimit the deposit limit to set
     */
    public void setDepositLimit(final double depositLimit) {
        this.depositLimit = depositLimit;
    }

    /**
     * Adds an associate to the account with a specific role.
     *
     * @param email the email of the associate
     * @param role  the role of the associate (manager or employee)
     */
    public void addAssociate(final String email, final String role) {
        if (associates.containsKey(email)) {
            throw new IllegalArgumentException("The user is already an associate of the account.");
        }
        if (!role.equalsIgnoreCase("manager")
                && !role.equalsIgnoreCase("employee")) {
            throw new IllegalArgumentException("Invalid role. Role must"
                    + " be either 'manager' or 'employee'.");
        }
        associates.put(email, role);
        if (role.equalsIgnoreCase("employee")) {
            spendingLimits.put(email, FIVE_HUNDRED_RON);
            depositLimits.put(email, FIVE_HUNDRED_RON);
        }
    }

    /**
     * Checks if a user is authorized for a specific action type.
     *
     * @param email      the email of the user
     * @param actionType the type of action (spend or deposit)
     * @return true if the user is authorized, false otherwise
     */
    public boolean isAuthorized(final String email, final String actionType) {
        if (email.equals(ownerEmail)) {
            return true;
        }
        String role = associates.get(email);
        if (role == null) {
            return false;
        }
        switch (role.toLowerCase()) {
            case "manager":
                return true;
            case "employee":
                return actionType.equals("spend") || actionType.equals("deposit");
            default:
                return false;
        }
    }

    /**
     * Updates the spending or deposit limit for an associate.
     *
     * @param email     the email of the associate
     * @param limitType the type of limit (spend or deposit)
     * @param amount    the new limit amount
     */
    public void updateLimit(final String email, final String limitType, final double amount) {
        if (!email.equals(ownerEmail)) {
            throw new IllegalArgumentException("Only the owner can update limits.");
        }
        if (limitType.equalsIgnoreCase("spend")) {
            spendingLimits.put(email, amount);
        } else if (limitType.equalsIgnoreCase("deposit")) {
            depositLimits.put(email, amount);
        } else {
            throw new IllegalArgumentException("Invalid limit type. Must be 'spend' or 'deposit'.");
        }
    }

    /**
     * Gets the role of an associate by email.
     *
     * @param email the email of the associate
     * @return the role of the associate, or "No role assigned" if not found
     */
    public String getRole(final String email) {
        return Objects.requireNonNullElse(associates.get(email), "No role assigned");
    }

    /**
     * Checks if a transaction can be performed by a user.
     *
     * @param email           the email of the user
     * @param amount          the amount of the transaction
     * @param transactionType the type of transaction (spend or deposit)
     * @return true if the transaction can be performed, false otherwise
     */
    public boolean canPerformTransaction(final String email, final double amount,
                                         final String transactionType) {
        if (email.equals(ownerEmail)) {
            return true;
        }
        if (!associates.containsKey(email)) {
            return false;
        }
        double limit = transactionType.equals("spend") ? spendingLimits.getOrDefault(email, 0.0)
                : depositLimits.getOrDefault(email, 0.0);
        return amount <= limit;
    }
}
