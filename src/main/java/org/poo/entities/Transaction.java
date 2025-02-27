package org.poo.entities;

import java.util.List;
import java.util.UUID;

/**
 * Represents a financial transaction, which can be processed in a payment system.
 */
public final class Transaction {

    private String email;
    private String id;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private String currency;
    private int timestamp;
    private String description;
    private TransactionType type;
    private String cardNumber;
    private String commerciant;
    private String error = "nesetat";
    private List<String> involvedAccounts;
    private double totalAmount;
    private TransactionStatus status;
    private List<Double> amountForUsers;
    private boolean isSpending = false;
    private boolean isDeposit = false;
    private String splitPaymentType;

    // Constructor
    public Transaction(final String email, final String fromAccount, final String toAccount,
                       final double amount, final String currency, final int timestamp,
                       final String description, final TransactionStatus status) {
        this(email, fromAccount, toAccount, amount, currency, timestamp, description,
                TransactionType.SEND_MONEY, null, null, status);
    }

    // Constructor for complex transactions
    public Transaction(final String email, final int timestamp, final String description,
                       final double totalAmount, final String currency, final double amount,
                       final List<String> involvedAccounts, final TransactionType type) {
        this.email = email;
        this.timestamp = timestamp;
        this.description = description;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.amount = amount;
        this.involvedAccounts = involvedAccounts;
        this.type = type;
        this.id = UUID.randomUUID().toString();  // Generate unique ID
    }

    // General constructor for custom use cases
    public Transaction(final String email, final String fromAccount, final String toAccount,
                       final double amount, final String currency, final int timestamp,
                       final String description, final TransactionType type,
                       final String cardNumber, final String commerciant,
                       final TransactionStatus status) {
        this.email = email;
        this.id = UUID.randomUUID().toString();  // Generate unique ID
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.description = description;
        this.type = type;
        this.cardNumber = cardNumber;
        this.commerciant = commerciant;
        this.status = status;
    }

    public Transaction(final String email, final String fromAccount, final String toAccount,
                       final double amount, final String currency, final int timestamp,
                       final String description, final TransactionType type,
                       final TransactionStatus status) {
        this(email, fromAccount, toAccount, amount, currency, timestamp,
                description, type, null, null, status);
    }

    // Constructor specific pentru tranzacții de tip split
        public Transaction(final String email,
                         final List<Double> amountForUsers,
                       final double totalAmount,
                       final String currency,
                       final String description,
                       final List<String> involvedAccounts,
                       final String splitPaymentType,
                        final TransactionType type,
                       final int timestamp) {
        this.email = email;
        this.totalAmount = totalAmount;
        this.amount = 0.0; // Suma individuală poate fi setată separat
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
        this.amountForUsers = amountForUsers;
        this.splitPaymentType = splitPaymentType;
        this.type = type;
        this.timestamp = timestamp;
        this.status = null;
    }


    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public String getError() {
        return error;
    }

    public List<String> getInvolvedAccounts() {
        return involvedAccounts;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public boolean isSpending() {
        return isSpending;
    }

    public boolean isDeposit() {
        return isDeposit;
    }

    public void setSpending(final boolean spending) {
        this.isSpending = spending;
    }

    public void setDeposit(final boolean deposit) {
        this.isDeposit = deposit;
    }

    public void setStatus(final TransactionStatus status) {
        this.status = status;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public void setInvolvedAccounts(final List<String> involvedAccounts) {
        this.involvedAccounts = involvedAccounts;
    }

    public void setTotalAmount(final double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<Double> getAmountForUsers() {
        return amountForUsers;
    }

    public void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }

    public void setAmount(final double amount) {
        this.amount = amount;
    }

    public void setSplitPaymentType(final String splitPaymentType) {
        this.splitPaymentType = splitPaymentType;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

}
