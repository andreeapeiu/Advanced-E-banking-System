package org.poo.entities;

import java.util.List;
import java.util.ArrayList;
import org.poo.entities.Account.Account;

/**
 * Represents a financial split between multiple accounts.
 */
public class Split {
    private final List<Account> accounts;
    private final List<Boolean> acceptedSplit;
    private final double amount;
    private final String currency;
    private final int timestamp;
    private final String splitPaymentType;
    private final List<Double> amountForUsers;

    /**
     * Constructor for creating a Split instance.
     *
     * @param accounts List of accounts involved in the split.
     * @param amount The total amount to be split.
     * @param currency The currency of the split.
     * @param timestamp The timestamp of the split.
     * @param splitPaymentType The type of the split payment.
     * @param amountForUsers The individual amounts allocated for each user.
     */
    public Split(final List<Account> accounts, final double amount,
                 final String currency, final int timestamp, final String splitPaymentType,
                 final List<Double> amountForUsers) {
        this.accounts = accounts;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.splitPaymentType = splitPaymentType;
        this.amountForUsers = amountForUsers;
        this.acceptedSplit = new ArrayList<>();

        // Initialize acceptedSplit with false
        for (int i = 0; i < accounts.size(); i++) {
            this.acceptedSplit.add(false);
        }
    }

    /**
     * Retrieves the list of accounts involved in the split.
     *
     * @return A list of accounts.
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Retrieves the total amount to be split.
     *
     * @return The total amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Retrieves the currency of the split.
     *
     * @return The currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Retrieves the timestamp of the split.
     *
     * @return The timestamp.
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieves the type of the split payment.
     *
     * @return The split payment type.
     */
    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    /**
     * Retrieves the list of amounts allocated for each user.
     *
     * @return A list of amounts for users.
     */
    public List<Double> getAmountForUsers() {
        return amountForUsers;
    }

    /**
     * Retrieves the list of acceptance statuses for the split.
     *
     * @return A list of booleans indicating acceptance.
     */
    public List<Boolean> getAcceptedSplit() {
        return acceptedSplit;
    }

    /**
     * Updates the acceptedSplit list for the specified account.
     *
     * @param email       The email of the user for whom to update acceptance.
     * @param accepted    The new value (true/false) for acceptance.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateAcceptedSplitByEmail(final String email, final boolean accepted) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getOwner().getEmail().equalsIgnoreCase(email)) {
                acceptedSplit.set(i, accepted);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all accounts have accepted the split.
     *
     * @return true if all accounts have accepted, false otherwise.
     */
    public boolean allAccepted() {
        for (final Boolean accepted : acceptedSplit) {
            if (!accepted) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a user has accepted the split.
     *
     * @param email The email of the user.
     * @return true if the user has accepted the split, false otherwise.
     */
    public boolean hasUserAccepted(final String email) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getOwner().getEmail().equalsIgnoreCase(email)) {
                return acceptedSplit.get(i); // Return the acceptance status
            }
        }
        throw new IllegalArgumentException("User with email "
                + email + " is not part of this split.");
    }
}
