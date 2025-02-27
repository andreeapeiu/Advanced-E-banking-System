package org.poo.services;

import java.util.List;
import java.util.Optional;
import org.poo.entities.Commerciants.CashbackType;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.repository.CommerciantsRepository;

/**
 * Service for managing commerciants.
 * <p>
 * This class is marked as final to indicate it is not intended for subclassing.
 * </p>
 */
public final class CommerciantsService {

    private final CommerciantsRepository commerciantsRepository;

    /**
     * Constructs a new CommerciantsService with the specified repository.
     *
     * @param commerciantsRepository the repository to use
     */
    public CommerciantsService(final CommerciantsRepository commerciantsRepository) {
        this.commerciantsRepository = commerciantsRepository;
    }

    /**
     * Retrieves all commerciants.
     *
     * @return a list of all commerciants
     */
    public List<Commerciant> getAllCommerciants() {
        return commerciantsRepository.findAll();
    }

    /**
     * Retrieves a commerciant by name (case-insensitive).
     *
     * @param name the name of the commerciant to look for
     * @return the commerciant found, or null if no match was found
     */
    public Commerciant getCommerciantByName(final String name) {
        List<Commerciant> allCommerciants = getAllCommerciants();
        for (Commerciant c : allCommerciants) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Retrieves a commerciant by ID.
     *
     * @param id the ID of the commerciant
     * @return the corresponding commerciant, or null if not found
     */
    public Commerciant getCommerciantById(final int id) {
        return commerciantsRepository.findById(id).orElse(null);
    }

    /**
     * Converts a cashback strategy string to a {@link CashbackType} enum.
     *
     * @param strategy the string representation of the cashback strategy
     * @return the corresponding {@link CashbackType}
     * @throws IllegalArgumentException if the strategy is invalid
     */
    private CashbackType parseCashbackStrategy(final String strategy) {
        for (CashbackType value : CashbackType.values()) {
            if (value.name().equalsIgnoreCase(strategy)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid cashback strategy: " + strategy);
    }

    /**
     * Adds a new commerciant to the repository.
     *
     * @param name             the name of the commerciant
     * @param id               the ID of the commerciant
     * @param account          the account details of the commerciant
     * @param type             the type/category of the commerciant
     * @param cashbackStrategy the cashback strategy as a string
     */
    public void addCommerciant(
            final String name,
            final int id,
            final String account,
            final String type,
            final String cashbackStrategy
    ) {
        CashbackType strategy = parseCashbackStrategy(cashbackStrategy);
        Commerciant commerciant = new Commerciant(id, name, account, type, strategy);
        commerciantsRepository.addCommerciant(commerciant);
    }

    /**
     * Deletes a commerciant by ID.
     *
     * @param id the ID of the commerciant to delete
     * @throws IllegalArgumentException if the commerciant is not found
     */
    public void deleteCommerciant(final int id) {
        Optional<Commerciant> commerciant = commerciantsRepository.findById(id);
        if (commerciant.isEmpty()) {
            throw new IllegalArgumentException("Commerciant not found.");
        }
        commerciantsRepository.deleteById(id);
    }
}
