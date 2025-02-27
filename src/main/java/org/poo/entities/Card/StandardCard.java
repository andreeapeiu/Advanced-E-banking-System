package org.poo.entities.Card;

/**
 * Represents a Standard Card, a typical type of card with a basic set of features.
 */
public class StandardCard extends Card {

    /**
     * Constructor to initialize a StandardCard with account IBAN and owner email.
     *
     * @param accountIban The IBAN associated with the account.
     * @param ownerEmail The email address of the card's owner.
     */
    public StandardCard(final String accountIban, final String ownerEmail) {
        // Creăm un card de tip Standard
        super(accountIban, ownerEmail, CardType.CLASSIC); // Cardul va avea tipul STANDARD
    }

    /**
     * Returns a string representation of the StandardCard, including card details.
     *
     * @return A string containing the card's details.
     */
    @Override
    public String toString() {
        return "StandardCard{"
                + "cardNumber='" + getCardNumber() + '\''
                + ", accountIban='" + getAccountIban() + '\''
                + ", ownerEmail='" + getOwnerEmail() + '\''
                + ", status=" + getStatus()
                + ", cardType=" + getCardType()
                + '}';
    }
}
