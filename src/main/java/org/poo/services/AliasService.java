package org.poo.services;

import org.poo.entities.accountAlias.Alias;
import org.poo.repository.AliasRepository;

/**
 * Service class for managing aliases.
 */
public final class AliasService {
    private final AliasRepository aliasRepository;

    /**
     * Constructor for AliasService.
     *
     * @param aliasRepository the repository for aliases.
     */
    public AliasService(final AliasRepository aliasRepository) {
        this.aliasRepository = aliasRepository;
    }

    /**
     * Adds a new alias to the repository.
     *
     * @param aliasName the alias name
     * @param iban      the IBAN associated with the alias
     * @param userEmail the email of the user who owns the alias
     */
    public void addAlias(final String aliasName, final String iban, final String userEmail) {
        if (aliasRepository.findAliasByAlias(aliasName) != null) {
            throw new IllegalArgumentException("Alias already exists: " + aliasName);
        }
        Alias alias = new Alias(aliasName, iban, userEmail);
        aliasRepository.addAlias(alias);
    }

    /**
     * Finds an alias by its name.
     *
     * @param aliasName the alias name
     * @return the alias object or null if not found
     */
    public Alias findByAlias(final String aliasName) {
        return aliasRepository.findAliasByAlias(aliasName);
    }

    /**
     * Finds an alias by its associated IBAN.
     *
     * @param iban the IBAN
     * @return the alias object or null if not found
     */
    public Alias findByIban(final String iban) {
        return aliasRepository.findByIban(iban);
    }

    /**
     * Retrieves the IBAN associated with a given alias.
     *
     * @param aliasName the alias name
     * @return the associated IBAN or null if alias not found
     */
    public String getAccountIBAN(final String aliasName) {
        Alias alias = aliasRepository.findAliasByAlias(aliasName);
        if (alias == null) {
            throw new IllegalArgumentException("Alias not found: " + aliasName);
        }
        return alias.getAccountIBAN();
    }

}
