package org.poo.repository;

import org.poo.entities.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing transaction records.
 */
public final class TransactionRepository {
    private final List<Transaction> transactions;

    /**
     * Constructs a new TransactionRepository with an empty list of transactions.
     */
    public TransactionRepository() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Saves a new transaction to the repository.
     *
     * @param transaction the transaction to be saved.
     */
    public void saveTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Retrieves all transactions from the repository.
     *
     * @return a list of all recorded transactions.
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Retrieves all transactions associated with a specific account.
     *
     * @param account the account for filtering transactions.
     * @return a list of transactions associated with the given account.
     */
    public List<Transaction> getTransactionsByAccount(final String account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (account.equals(tx.getFromAccount()) || account.equals(tx.getToAccount())) {
                result.add(tx);
            }
        }
        return result;
    }


    /**
     * Records a new transaction to the repository.
     *
     * @param transaction the transaction to be recorded.
     */
    public void recordTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }
}
