package org.poo.entities;

import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user with personal details and a list of accounts.
 */
public final class User {
    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String occupation;
    private String plan;
    private final List<Account> accounts;
    private int eligiblePaymentsForGold;
    private double totalSpent = 0;

    /**
     * Constructor for User.
     *
     * @param firstName  the first name of the user.
     * @param lastName   the last name of the user.
     * @param email      the email of the user.
     * @param birthDate  the birth date of the user.
     * @param occupation the occupation of the user.
     */
    public User(final String firstName, final String lastName,
                final String email, final String birthDate,
                final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.plan = occupation.equals("student") ? "student" : "standard";
        this.accounts = new ArrayList<>();
    }

    /**
     * Gets the first name of the user.
     *
     * @return the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name to set.
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the user.
     *
     * @return the last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name to set.
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email of the user.
     *
     * @return the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user.
     *
     * @param email the email to set.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Gets the birth date of the user.
     *
     * @return the birth date.
     */
    public String getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the birth date of the user.
     *
     * @param birthDate the birth date to set.
     */
    public void setBirthDate(final String birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Gets the occupation of the user.
     *
     * @return the occupation.
     */
    public String getOccupation() {
        return occupation;
    }

    /**
     * Sets the occupation of the user.
     *
     * @param occupation the occupation to set.
     */
    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    /**
     * Gets the plan of the user.
     *
     * @return the plan.
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Gets the total spent by the user.
     *
     * @return the total spent.
     */
    public double getTotalSpent() {
        return totalSpent;
    }

    /**
     * Sets the total spent by the user.
     *
     * @param totalSpent the total amount spent to set.
     */
    public void setTotalSpent(final double totalSpent) {
        this.totalSpent = totalSpent;
    }

    /**
     * Sets the plan of the user.
     *
     * @param plan the plan to set.
     */
    public void setPlan(final String plan) {
        this.plan = (plan == null || plan.isEmpty()) ? "standard" : plan;
    }

    /**
     * Gets the accounts of the user.
     *
     * @return the list of accounts.
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Adds an account to the user.
     *
     * @param account the account to add.
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * Removes an account from the user.
     *
     * @param account the account to remove.
     */
    public void removeAccount(final Account account) {
        accounts.remove(account);
    }

    /**
     * Gets all cards associated with the user's accounts.
     *
     * @return a list of cards.
     */
    public List<Card> getCards() {
        List<Card> cards = new ArrayList<>();
        for (Account account : accounts) {
            cards.addAll(account.getCards());
        }
        return cards;
    }

    /**
     * Gets the number of eligible payments for upgrading to Gold.
     *
     * @return the number of eligible payments.
     */
    public int getEligiblePaymentsForGold() {
        return eligiblePaymentsForGold;
    }

    /**
     * Sets the number of eligible payments for upgrading to Gold.
     *
     * @param eligiblePaymentsForGold the number of eligible payments to set.
     */
    public void setEligiblePaymentsForGold(final int eligiblePaymentsForGold) {
        this.eligiblePaymentsForGold = eligiblePaymentsForGold;
    }

    /**
     * Increments the number of eligible payments for Gold.
     */
    public void incrementEligiblePayments() {
        this.eligiblePaymentsForGold++;
    }

    /**
     * Resets the eligible payments count to zero.
     */
    public void resetEligiblePayments() {
        this.eligiblePaymentsForGold = 0;
    }
}
