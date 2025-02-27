package org.poo.entities.Card;

/**
 * Represents a One-Time Card, a specific type of card that is valid for a single transaction.
 */
public class OneTimeCard extends Card {

    /**
     * Constructor to initialize a OneTimeCard.
     *
     * @param accountIban The IBAN associated with the account.
     * @param ownerEmail The email address of the card's owner.
     */
    public OneTimeCard(final String accountIban, final String ownerEmail) {
        super(accountIban, ownerEmail, CardType.ONE_TIME); // Cardul va avea tipul ONE_TIME
    }

    /**
     * Regenerates a new OneTimeCard with the given account IBAN and owner email.
     *
     * @param accountIban The IBAN associated with the account.
     * @param ownerEmail The email address of the card's owner.
     * @return A new instance of OneTimeCard.
     */
    public static OneTimeCard regenerate(final String accountIban, final String ownerEmail) {
        return new OneTimeCard(accountIban, ownerEmail);
    }

    /**
     *
     * @return A string containing the card's details.
     */
    @Override
    public String toString() {
        return "OneTimeCard{"
                + "cardNumber='" + getCardNumber() + '\''
                + ", accountIban='" + getAccountIban() + '\''
                + ", ownerEmail='" + getOwnerEmail() + '\''
                + ", status=" + getStatus()
                + ", cardType=" + getCardType()
                + '}';
    }
}
