package org.poo.repository;

import org.poo.entities.Account.Account;
import org.poo.entities.Split;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing Split entities.
 * Provides methods to add, retrieve, update, and delete splits.
 */
public class SplitsRepository {

    private final List<Split> splits;
    private final TransactionRepository transactionRepository;

    /**
     * Default constructor initializing an empty list of splits.
     */
    public SplitsRepository(final TransactionRepository transactionRepository) {
        this.splits = new ArrayList<>();
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns all splits.
     * @return a list of all splits.
     */
    public List<Split> getAllSplits() {
        return splits;
    }

    /**
     * Adds a new Split to the repository.
     *
     * @param split the Split to add.
     */
    public void addSplit(final Split split) {
        splits.add(split);
    }

    /**
     * Removes a split from the repository.
     *
     * @param split the split to remove.
     */
    public void removeSplit(final Split split) {
        Transaction foundTransaction = null;
        for (Transaction transaction : transactionRepository.getAllTransactions()) {
            if (transaction.getTimestamp() == split.getTimestamp()) {
                foundTransaction = transaction;
                // Set as succesful so it will appear in printTransactions
                foundTransaction.setStatus(TransactionStatus.successful);
            }
        }
        if (foundTransaction == null) {
            throw new IllegalArgumentException("No transaction found for the given split.");
        }
        splits.remove(split);
    }

    /**
     * Updates the accepted status for a specific account in a split.
     * Returns the updated split.
     *
     * @param email         the email of the user accepting the split.
     * @param accepted      the acceptance status.
     * @throws IllegalArgumentException if no matching split or account is found.
     */
    public Split updateAcceptedStatus(final String email, final boolean accepted) {
        for (Split split : splits) {
            for (Account account : split.getAccounts()) {
                if (account.getOwner().getEmail().equalsIgnoreCase(email)) {
                    // Verify if the user has already accepted the split
                    if (!split.hasUserAccepted(email)) {
                        boolean updated = split.updateAcceptedSplitByEmail(email, accepted);
                        if (!updated) {
                            throw new IllegalArgumentException("User with email "
                                    + email + " is not part of the split.");
                        }
                        return split;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all Splits.
     *
     * @return a list of all Splits.
     */
    public List<Split> findAll() {
        return new ArrayList<>(splits);
    }


}
