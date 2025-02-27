package org.poo.repository;

import org.poo.entities.Commerciants.Commerciant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing commerciants.
 * Provides methods to add, retrieve, update, and delete commerciants.
 */
public class CommerciantsRepository {

    private final List<Commerciant> commerciants;

    /**
     * Default constructor initializing an empty list of commerciants.
     */
    public CommerciantsRepository() {
        this.commerciants = new ArrayList<>();
    }

    /**
     * Adds a new commerciant to the repository.
     *
     * @param commerciant the commerciant to add.
     */
    public void addCommerciant(final Commerciant commerciant) {
        commerciants.add(commerciant);
    }

    /**
     * Retrieves a commerciant by ID.
     *
     * @param id the ID of the commerciant to retrieve.
     * @return an Optional containing the commerciant if found, or empty if not.
     */
    public Optional<Commerciant> findById(final int id) {
        return commerciants.stream()
                .filter(commerciant -> commerciant.getId() == id)
                .findFirst();
    }

    /**
     * Retrieves all commerciants.
     *
     * @return a list of all commerciants.
     */
    public List<Commerciant> findAll() {
        return new ArrayList<>(commerciants);
    }

    /**
     * Updates an existing commerciant.
     *
     * @param updatedCommerciant the commerciant with updated details.
     * @throws IllegalArgumentException if the commerciant does not exist.
     */
    public void updateCommerciant(final Commerciant updatedCommerciant) {
        Commerciant existingCommerciant = findById(updatedCommerciant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Commerciant not found."));

        existingCommerciant.setName(updatedCommerciant.getName());
        existingCommerciant.setAccount(updatedCommerciant.getAccount());
        existingCommerciant.setType(updatedCommerciant.getType());
        existingCommerciant.setCashbackStrategy(updatedCommerciant.getCashbackStrategy());
    }

    /**
     * Deletes a commerciant by ID.
     *
     * @param id the ID of the commerciant to delete.
     * @throws IllegalArgumentException if the commerciant does not exist.
     */
    public void deleteById(final int id) {
        Commerciant commerciant = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commerciant not found."));
        commerciants.remove(commerciant);
    }
}
