package org.poo.repository;

import org.poo.entities.Account.Account;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for managing accounts.
 */
public final class AccountRepository {
    private final List<Account> accounts;

    public AccountRepository() {
        this.accounts = new ArrayList<>();
    }

    /**
     * Adds an account to the repository.
     *
     * @param account the account to be added. Cannot be null.
     * @throws IllegalArgumentException if the account is null.
     */
    public void addAccount(final Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        accounts.add(account);
    }

    /**
     * Finds an account by its IBAN.
     *
     * @param iban the IBAN of the account to be found.
     * @return the account matching the IBAN, or null if no account is found.
     */
    public Account findAccountByIban(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().equals(iban)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Deletes an account from the repository by its IBAN.
     *
     * @param iban the IBAN of the account to be deleted.
     */
    public void deleteAccount(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().equals(iban)) {
                accounts.remove(account);
                break;
            }
        }
    }

    /**
     * Gets all accounts from the repository.
     *
     * @return a list of all accounts in the repository.
     */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Gets an account by its IBAN.
     *
     * @param iban the IBAN of the account to be retrieved. Cannot be null.
     * @return the account matching the IBAN, or null if no account is found.
     */
    public Account getAccountByIban(final String iban) {
        for (Account acc : accounts) {
            if (acc.getIban().equals(iban)) {
                return acc;
            }
        }
        return null;
    }

    /**
     * Updates an existing account in the repository.
     *
     * @param account the account to be updated. Cannot be null.
     * @throws IllegalArgumentException if the account is null or does not exist in the repository.
     */
    public void updateAccount(final Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getIban().equals(account.getIban())) {
                accounts.set(i, account);
                return;
            }
        }

        throw new IllegalArgumentException("Account not found in repository.");
    }

}
