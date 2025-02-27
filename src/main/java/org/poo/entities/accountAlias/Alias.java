package org.poo.entities.accountAlias;

import java.util.Objects;

/**
 * Represents an alias associated with a bank account. The alias can be used
 * to reference the account using a user-friendly name.
 */
public class Alias {

    private String userEmail;
    private String aliasName;
    private String accountIBAN;

    /**
     * Constructs an Alias object with the given alias name, account IBAN, and user email.
     *
     * @param aliasName the alias name
     * @param accountIBAN the IBAN of the associated account
     * @param userEmail the email of the user associated with this alias
     */
    public Alias(final String aliasName, final String accountIBAN, final String userEmail) {
        this.aliasName = aliasName;
        this.accountIBAN = accountIBAN;
        this.userEmail = userEmail;
    }

    // Getters and Setters with final parameters
    /**
     * Gets the user email associated with the alias.
     *
     * @return the user email
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the user email for the alias.
     *
     * @param userEmail the new user email
     */
    public void setUserEmail(final String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Gets the alias name.
     *
     * @return the alias name
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Sets the alias name.
     *
     * @param aliasName the new alias name
     */
    public void setAliasName(final String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * Gets the account IBAN associated with the alias.
     *
     * @return the account IBAN
     */
    public String getAccountIBAN() {
        return accountIBAN;
    }

    /**
     * Sets the account IBAN for the alias.
     *
     * @param accountIBAN the new account IBAN
     */
    public void setAccountIBAN(final String accountIBAN) {
        this.accountIBAN = accountIBAN;
    }

    /**
     * Provides a string representation of the Alias object.
     *
     * @return a string representing the Alias
     */
    @Override
    public String toString() {
        return "Alias{"
                + "userEmail='" + userEmail + '\''
                + ", aliasName='" + aliasName + '\''
                + ", accountIBAN='" + accountIBAN + '\''
                + '}';
    }

    /**
     * Compares the Alias object with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Alias alias = (Alias) o;
        if (!aliasName.equals(alias.aliasName)) {
            return false;
        }
        if (!accountIBAN.equals(alias.accountIBAN)) {
            return false;
        }
        return true;
    }

    /**
     * Generates a hash code for the Alias object.
     *
     * @return a hash code value for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(aliasName, accountIBAN);
    }
}
