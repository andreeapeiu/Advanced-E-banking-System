package org.poo.entities;

import java.util.Objects;

/**
 * Represents the exchange rate between two currencies.
 */
public final class ExchangeRate {
    private String fromCurrency;
    private String toCurrency;
    private double rate;

    /**
     * Constructor to initialize an exchange rate.
     *
     * @param fromCurrency The currency to convert from.
     * @param toCurrency The currency to convert to.
     * @param rate The exchange rate.
     */
    public ExchangeRate(final String fromCurrency, final String toCurrency, final double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }

    /**
     * Get the currency to convert from.
     *
     * @return the fromCurrency
     */
    public String getFromCurrency() {
        return fromCurrency;
    }

    /**
     * Set the currency to convert from.
     *
     * @param fromCurrency the fromCurrency to set
     */
    public void setFromCurrency(final String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    /**
     * Get the currency to convert to.
     *
     * @return the toCurrency
     */
    public String getToCurrency() {
        return toCurrency;
    }

    /**
     * Set the currency to convert to.
     *
     * @param toCurrency the toCurrency to set
     */
    public void setToCurrency(final String toCurrency) {
        this.toCurrency = toCurrency;
    }

    /**
     * Get the exchange rate.
     *
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * Set the exchange rate.
     *
     * @param rate the rate to set
     */
    public void setRate(final double rate) {
        this.rate = rate;
    }

    /**
     * Convert an amount from the fromCurrency to the toCurrency.
     *
     * @param amount The amount to convert.
     * @return The converted amount.
     */
    public double convert(final double amount) {
        return amount * rate;
    }

    /**
     * Return a string representation of the exchange rate.
     *
     * @return A string representation of the ExchangeRate.
     */
    @Override
    public String toString() {
        return "ExchangeRate{"
                + "fromCurrency='" + fromCurrency + '\''
                + ", toCurrency='" + toCurrency + '\''
                + ", rate=" + rate
                + '}';
    }

    /**
     * Check equality based on the fromCurrency, toCurrency, and rate.
     *
     * @param o Object to compare
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExchangeRate that = (ExchangeRate) o;
        return Double.compare(that.rate, rate) == 0
                && Objects.equals(fromCurrency, that.fromCurrency)
                && Objects.equals(toCurrency, that.toCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate);
    }
}
