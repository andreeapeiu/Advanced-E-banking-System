package org.poo.entities.Account;

import org.poo.entities.User;
import org.poo.repository.AliasRepository;
import org.poo.repository.CardRepository;

public class SavingsAccount extends Account {

    private double interestRate;

    /**
     * Constructor for the SavingsAccount class.
     *
     * @param iban the account IBAN
     * @param currency the currency of the account
     * @param interestRate the interest rate for the savings account
     * @param owner the owner of the account
     * @param cardRepository the repository for cards
     * @param aliasrepo the repository for aliases
     */
    public SavingsAccount(final String iban, final String currency, final double interestRate,
                          final User owner, final CardRepository cardRepository,
                          final AliasRepository aliasrepo) {
        super(iban, currency, owner, cardRepository, aliasrepo);
        this.interestRate = interestRate;
        setAccType(AccountType.savings);
    }

    /**
     * Gets the interest rate for the savings account.
     *
     * @return the interest rate
     */
    @Override
    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Sets the interest rate for the savings account.
     *
     * @param interestRate the new interest rate
     */
    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Allows a withdrawal from the savings account.
     *
     * @param amount   the amount to withdraw
     * @return true if the withdrawal is successful, false otherwise
     */
    public boolean withdrawFromSavings(final double amount) {
        if (getBalance() < amount) {
            return false;
        }
        setBalance(getBalance() - amount);
        return true;
    }

    /**
     * Returns the account type as a string.
     *
     * @return the account type (savings)
     */
    @Override
    public String getAccountType() {
        return "savings";
    }
}
