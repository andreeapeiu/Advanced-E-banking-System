package org.poo.repository;

import org.poo.entities.accountAlias.Alias;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for managing aliases associated with accounts.
 */
public final class AliasRepository {
    private final List<Alias> aliases = new ArrayList<>();

    /**
     * Adds an alias to the repository.
     *
     * @param alias the alias to be added.
     */
    public void addAlias(final Alias alias) {
        aliases.add(alias);
    }

    /**
     * Finds an alias by its name.
     *
     * @param aliasName the name of the alias to be found.
     * @return the alias with the given name, or null if no alias is found.
     */
    public Alias findAliasByAlias(final String aliasName) {
        for (Alias alias : aliases) {
            if (alias.getAliasName().equals(aliasName)) {
                return alias;
            }
        }
        return null;
    }

    /**
     * Finds an alias associated with the given IBAN.
     *
     * @param iban the IBAN to be matched with an alias.
     * @return the alias with the corresponding IBAN, or null if no alias is found.
     */
    public Alias findByIban(final String iban) {
        for (Alias alias : aliases) {
            if (alias.getAccountIBAN().equals(iban)) {
                return alias;
            }
        }
        return null;
    }

    /**
     * Removes an alias from the repository.
     *
     * @param aliasName the name of the alias to be removed.
     * @return true if the alias was removed, false if no alias was found with the given name.
     */
    public boolean removeAlias(final String aliasName) {
        for (Alias alias : aliases) {
            if (alias.getAliasName().equals(aliasName)) {
                aliases.remove(alias);
                return true;
            }
        }
        return false;
    }

}
