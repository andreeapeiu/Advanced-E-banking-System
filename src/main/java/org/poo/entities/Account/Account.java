package org.poo.entities.Account;

import org.poo.entities.Card.Card;
import org.poo.entities.User;
import org.poo.entities.accountAlias.Alias;
import org.poo.repository.AliasRepository;
import org.poo.repository.CardRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class representing an Account.
 */
public abstract class Account {

    private static final int THRESHOLD_100 = 100;
    private static final int THRESHOLD_300 = 300;
    private static final int THRESHOLD_500 = 500;
    private static final double CASHBACK_GOLD_100 = 0.5;
    private static final double CASHBACK_SILVER_100 = 0.3;
    private static final double CASHBACK_STANDARD_100 = 0.1;
    private static final double CASHBACK_GOLD_300 = 0.55;
    private static final double CASHBACK_SILVER_300 = 0.4;
    private static final double CASHBACK_STANDARD_300 = 0.2;
    private static final double CASHBACK_GOLD_500 = 0.7;
    private static final double CASHBACK_SILVER_500 = 0.5;
    private static final double CASHBACK_STANDARD_500 = 0.25;

    private String iban;
    private double balance;
    private String currency;
    private User owner;
    private AccountType accType;
    private List<Card> cards;
    private double minimumBalance;
    private boolean hasMinimumBalance;
    private double blockAmount = 0.0;
    private final CardRepository cardRepository;
    private final AliasRepository aliasRepository;

    private Map<String, Integer> transactionsPerCommerciant;
    private Map<String, Double> totalSpentPerCommerciant;
    private Set<String> discountsPerCategory;
    private int nrOfTransactionsCount;
    private Map<String, Double> moneySpentAtCommerciantsWithCashbackStrategyThreshold;

    /**
     * Constructor for the Account class.
     *
     * @param iban the account IBAN
     * @param currency the currency of the account
     * @param owner the owner of the account
     * @param cardRepository the repository for cards
     * @param aliasrepo the repository for aliases
     */
    public Account(final String iban, final String currency, final User owner,
                   final CardRepository cardRepository, final AliasRepository aliasrepo) {
        this.iban = iban;
        this.balance = 0.0;
        this.currency = currency;
        this.cards = new ArrayList<>();
        this.owner = owner;
        this.hasMinimumBalance = false;
        this.cardRepository = cardRepository;
        this.aliasRepository = aliasrepo;

        this.transactionsPerCommerciant = new HashMap<>();
        this.totalSpentPerCommerciant = new HashMap<>();
        this.discountsPerCategory = new HashSet<>();
        this.nrOfTransactionsCount = 0;

        this.moneySpentAtCommerciantsWithCashbackStrategyThreshold = new HashMap<>();
    }

    /**
     * Gets the number of transactions per merchant.
     *
     * @return a map where the keys are merchant names and the values are the nr of transactionS
     */
    public Map<String, Integer> getNrOfTransactionsPerCommerciants() {
        return transactionsPerCommerciant;
    }

    /**
     * Sets the number of transactions per merchant.
     *
     * @param newMap a map where the keys are merchant names and the
     *               values are the number of transactions.
     */
    public void setNrOfTransactionsPerCommerciants(final Map<String, Integer> newMap) {
        this.transactionsPerCommerciant = newMap;
    }

    /**
     * Gets the money spent at merchants with a cashback strategy threshold.
     *
     * @return a map where the keys are merchant names and the values are the total money spent.
     */
    public Map<String, Double> getMoneySpentAtCommerciantsWithCashbackStrategyThreshold() {
        return moneySpentAtCommerciantsWithCashbackStrategyThreshold;
    }

    /**
     * Sets the money spent at merchants with a cashback strategy threshold.
     *
     * @param newMap a map where the keys are merchant names and the values
     * are the total money spent.
     */
    public void setMoneySpentAtCommerciantsWithCashbackStrategyThreshold(
            final Map<String, Double> newMap) {
        this.moneySpentAtCommerciantsWithCashbackStrategyThreshold = newMap;
    }

    /**
     * Sets the blocked amount for the account.
     *
     * @param amount the amount to be blocked.
     */
    public void setBlockAmount(final double amount) {
        this.blockAmount = amount;
    }

    /**
     * Gets the blocked amount for the account.
     *
     * @return the blocked amount.
     */
    public double getBlockAmount() {
        return blockAmount;
    }

    /**
     * Gets the IBAN of the account.
     *
     * @return the IBAN.
     */
    public String getIban() {
        return iban;
    }

    /**
     * Sets the IBAN of the account.
     *
     * @param iban the new IBAN.
     */
    public void setIban(final String iban) {
        this.iban = iban;
    }

    /**
     * Gets the balance of the account.
     *
     * @return the balance.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance of the account.
     *
     * @param balance the new balance.
     */
    public void setBalance(final double balance) {
        this.balance = balance;
    }

    /**
     * Gets the currency of the account.
     *
     * @return the currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency of the account.
     *
     * @param currency the new currency.
     */
    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    /**
     * Gets the owner of the account.
     *
     * @return the owner.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the account.
     *
     * @param owner the new owner.
     */
    public void setOwner(final User owner) {
        this.owner = owner;
    }

    /**
     * Gets the account type.
     *
     * @return the account type.
     */
    public AccountType getAccType() {
        return accType;
    }

    /**
     * Sets the account type.
     *
     * @param accType the new account type.
     */
    public void setAccType(final AccountType accType) {
        this.accType = accType;
    }

    /**
     * Gets the list of cards associated with the account.
     *
     * @return the list of cards.
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Gets the email of the account owner.
     *
     * @return the owner's email.
     */
    public String getEmail() {
        return this.getOwner().getEmail();
    }

    /**
     * Checks if the account has a minimum balance requirement.
     *
     * @return true if the account has a minimum balance requirement, false otherwise.
     */
    public boolean getHasMinimumBalance() {
        return hasMinimumBalance;
    }

    /**
     * Sets whether the account has a minimum balance requirement.
     *
     * @param hasMinimumBalance true if the account should have a minimum balance requirement
     */
    public void setHasMinimumBalance(final boolean hasMinimumBalance) {
        this.hasMinimumBalance = hasMinimumBalance;
    }

    /**
     * Adds a card to the account.
     *
     * @param card the card to be added.
     */
    public void addCard(final Card card) {
        this.cards.add(card);
    }

    /**
     * Deposits an amount to the account.
     *
     * @param amount the amount to be deposited.
     */
    public void deposit(final double amount) {
        this.balance += amount;
    }

    /**
     * Withdraws an amount from the account.
     *
     * @param acc the account from which to withdraw.
     * @param amount the amount to be withdrawn.
     */
    public void withdraw(final Account acc, final double amount) {
        String plan = acc.owner.getPlan();
        double commission = 0.0;

        if (plan.equals("standard")) {
            commission = CASHBACK_STANDARD_300 / THRESHOLD_100 * amount;
        } else if (plan.equals("silver")) {
            if (amount >= THRESHOLD_500) {
                commission = CASHBACK_STANDARD_100 / THRESHOLD_100 * amount;
            }
        }

        if (acc.balance < amount + commission) {
            // Insufficient balance
            return;
        }

        acc.balance -= (amount + commission);
    }

    /**
     * Sets the minimum balance for the account.
     *
     * @param amount the minimum balance amount.
     */
    public void setMinimumBalance(final double amount) {
        this.minimumBalance = amount;
    }

    /**
     * Gets the minimum balance for the account.
     *
     * @return the minimum balance.
     */
    public double getMinimumBalance() {
        return minimumBalance;
    }

    /**
     * Sets an alias for the account.
     *
     * @param email the email associated with the alias.
     * @param accIban the IBAN of the account associated with the alias.
     * @param alias the alias name.
     */
    public void setAlias(final String email, final String accIban, final String alias) {
        Alias a = new Alias(email, accIban, alias);
        aliasRepository.addAlias(a);
    }

    /**
     * Gets a card by its number.
     *
     * @param number the card number.
     * @return the card associated with the given number.
     */
    public Card getCard(final String number) {
        return cardRepository.getCardByNumber(number);
    }

    /**
     * Abstract method to get the type of the account.
     *
     * @return the type of the account.
     */
    public abstract String getAccountType();

    /**
     * Abstract method to get the interest rate of the account.
     *
     * @return the interest rate of the account.
     */
    public abstract double getInterestRate();

    /**
     * Returns a string representation of the account.
     *
     * @return a string representation of the account.
     */
    @Override
    public String toString() {
        return "Account{"
                + "iban='" + iban + '\''
                + ", balance=" + balance
                + ", currency='" + currency + '\''
                + ", owner=" + owner.getEmail()
                + ", cards=" + cards.size()
                + '}';
    }

}
