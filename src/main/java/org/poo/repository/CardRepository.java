package org.poo.repository;

import org.poo.entities.Card.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing cards associated with accounts.
 */
public final class CardRepository {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Saves a new card to the repository.
     *
     * @param card the card to be saved.
     */
    public void saveCard(final Card card) {
        cards.add(card);
    }

    /**
     * Retrieves a list of cards associated with a specific account.
     *
     * @param accountIban the IBAN of the account whose cards I need.
     * @return a list of cards associated with the given account IBAN.
     */
    public List<Card> getCardsByAccount(final String accountIban) {
        List<Card> result = new ArrayList<>();
        for (Card card : cards) {
            if (card.getAccountIban().equals(accountIban)) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * Retrieves a card by its card number.
     *
     * @param number the number of the card to be retrieved.
     * @return the card associated with the given number, or null if not found.
     */
    public Card getCardByNumber(final String number) {
        for (Card card : cards) {
            if (card.getNumber().equals(number)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Deletes a card from the repository based on its card number.
     *
     * @param number the number of the card to be deleted.
     */
    public void deleteCard(final String number) {
        for (Card card : cards) {
            if (card.getNumber().equals(number)) {
                cards.remove(card);
                break;
            }
        }
    }
}
