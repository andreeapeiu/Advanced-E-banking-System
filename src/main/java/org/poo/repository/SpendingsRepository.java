package org.poo.repository;

import org.poo.entities.spendings.Spending;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing spending records.
 */
public final class SpendingsRepository {
    private final List<Spending> spendings;

    /**
     * Constructs a new SpendingsRepository with an empty list of spendings.
     */
    public SpendingsRepository() {
        this.spendings = new ArrayList<>();
    }

    /**
     * Records a new spending into the repository.
     *
     * @param spending the spending to be recorded.
     */
    public void recordSpending(final Spending spending) {
        spendings.add(spending);
    }


    /**
     * Retrieves all spendings associated with a specific IBAN.
     *
     * @param iban the IBAN for filtering spendings.
     *
     * @return a list of spendings associated with the given IBAN.
     */
    public List<Spending> getSpendingsByIban(final String iban) {
        List<Spending> result = new ArrayList<>();
        for (Spending sp : spendings) {
            if (sp.getIban().equals(iban)) {
                result.add(sp);
            }
        }
        return result;
    }

}
