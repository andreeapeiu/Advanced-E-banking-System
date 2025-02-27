package org.poo.entities.spendings;

public final class Spending {
    private String email;
    private String commerciantName;
    private double totalAmount;
    private int timestamp;
    private String iban;
    private String currency; // Adăugat pentru conversie

    // Constructor
    public Spending(final String email, final String commerciantName, final double totalAmount,
                    final int timestamp, final String iban, final String currency) {
        this.email = email;
        this.commerciantName = commerciantName;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.iban = iban;
        this.currency = currency; // Inițializat
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getCommerciantName() {
        return commerciantName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(final double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getLastTimestamp() {
        return timestamp;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency; // Getter pentru currency
    }

    public void setCurrency(final String currency) {
        this.currency = currency; // Setter pentru currency
    }

    @Override
    public String toString() {
        return "Spending{"
                + "email='" + email + '\''
                + ", commerciantName='" + commerciantName + '\''
                + ", totalAmount=" + totalAmount
                + ", timestamp=" + timestamp
                + ", iban='" + iban + '\''
                + ", currency='" + currency + '\''
                + '}';
    }
}
