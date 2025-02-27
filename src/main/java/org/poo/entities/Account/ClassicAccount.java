package org.poo.entities.Account;

import org.poo.entities.User;
import org.poo.repository.AliasRepository;
import org.poo.repository.CardRepository;

/**
 * Represents a classic account type. This class extends the abstract Account class.
 */
public class ClassicAccount extends Account {

    // Make interestRate private and provide getter and setter
    private double interestRate;

    /**
     * Constructor for ClassicAccount class.
     *
     * @param iban the account IBAN
     * @param currency the currency of the account
     * @param interestRate the interest rate for the account
     * @param owner the owner of the account
     * @param cardRepository the repository for cards
     * @param aliasrepo the repository for aliases
     */
    public ClassicAccount(final String iban, final String currency, final double interestRate,
                          final User owner, final CardRepository cardRepository,
                          final AliasRepository aliasrepo) {
        super(iban, currency, owner, cardRepository, aliasrepo);
        this.interestRate = interestRate;
        setAccType(AccountType.classic);
    }

    /**
     * Gets the interest rate for the classic account.
     *
     * @return the interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Sets the interest rate for the classic account.
     *
     * @param interestRate the new interest rate
     */
    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Returns the account type as a string.
     *
     * @return the account type (classic)
     */
    @Override
    public String getAccountType() {
        return "classic";
    }

}
