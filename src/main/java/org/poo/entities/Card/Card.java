package org.poo.entities.Card;

import org.poo.utils.Utils;

/**
 * Represents a Card with associated account, owner email, type, and status.
 */
public class Card {
    private String cardNumber; // Card number
    private final String accountIban; // IBAN
    private String ownerEmail;
    private CardStatus status; // Card status (ACTIVE, FROZEN)
    private CardType cardType; // Card type (CLASSIC, ONE_TIME)

    /**
     * Constructor to initialize a card with account IBAN, owner email, and card type.
     *
     * @param accountIban IBAN of the associated account.
     * @param ownerEmail Email address of the card owner.
     * @param cardType Type of the card
     */
    public Card(final String accountIban, final String ownerEmail, final CardType cardType) {
        this.cardNumber = Utils.generateCardNumber();
        this.accountIban = accountIban;
        this.ownerEmail = ownerEmail;
        this.cardType = cardType;
        this.status = CardStatus.active;
    }

    /**
     * Gets the card number.
     *
     * @return The card number.
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Gets the associated IBAN of the account.
     *
     * @return The account IBAN.
     */
    public String getAccountIban() {
        return accountIban;
    }

    /**
     * Gets the owner's email address.
     *
     * @return The owner's email.
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Sets the owner's email address.
     *
     * @param ownerEmail The new email address.
     */
    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * Gets the current status of the card.
     *
     * @return The card status (e.g., ACTIVE, FROZEN).
     */
    public CardStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the card.
     *
     * @param status The new card status.
     */
    public void setStatus(final CardStatus status) {
        this.status = status;
    }

    /**
     * Gets the type of the card.
     *
     * @return The card type (e.g., CLASSIC, ONE_TIME).
     */
    public CardType getCardType() {
        return cardType;
    }

    /**
     * Sets the type of the card.
     *
     * @param type The new card type.
     */
    public void setCardType(final CardType type) {
        this.cardType = type;
    }

    /**
     * Gets the card number
     *
     * @return The card number.
     */
    public String getNumber() {
        return cardNumber;
    }

    /**
     * Sets a new card number.
     *
     * @param card The card whose number is to be updated.
     */
    public void setCardNumber(final Card card) {
        card.cardNumber = Utils.generateCardNumber();
    }

    /**
     * Returns a string representation of the card.
     *
     * @return A string containing card details.
     */
    @Override
    public String toString() {
        return "Card{"
                + "cardNumber='" + cardNumber + '\''
                + ", accountIban='" + accountIban + '\''
                + ", ownerEmail='" + ownerEmail + '\''
                + ", status=" + status
                + ", cardType=" + cardType
                + '}';
    }
}
