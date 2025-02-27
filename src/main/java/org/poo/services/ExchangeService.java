package org.poo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;

/**
 * Singleton service for handling currency exchange rates and conversions.
 * Provides methods for adding exchange rates and performing currency conversions.
 */
public final class ExchangeService {
    private static final Map<String, Map<String, Double>> EXCHANGE_RATE_MAP = new HashMap<>();
    private static ExchangeService instance;

    // Private constructor to prevent instantiation
    private ExchangeService() {

    }

    /**
     * Returns the singleton instance of ExchangeService.
     *
     * @return the instance of ExchangeService
     */
    public static ExchangeService getInstance() {
        if (instance == null) {
            instance = new ExchangeService();
        }
        return instance;
    }

    /**
     * Resets the instance of ExchangeService, clearing the exchange rates.
     */
    public static void resetInstance() {
        if (instance != null) {
            EXCHANGE_RATE_MAP.clear(); // Clear exchange rates
        }
    }

    /**
     * Adds an exchange rate between two currencies.
     *
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @param rate the exchange rate
     */
    public void addExchangeRate(final String fromCurrency,
                                final String toCurrency, final double rate) {

        // add in Hashmap rates and the invers rates from input
        EXCHANGE_RATE_MAP.computeIfAbsent(fromCurrency, k -> new HashMap<>()).put(toCurrency, rate);
        EXCHANGE_RATE_MAP.computeIfAbsent(toCurrency, k -> new HashMap<>())
                .put(fromCurrency, 1 / rate); // Inverse rate
    }

    /**
     * Converts an amount from one currency to another using the available exchange rates.
     *
     * @param amount the amount to convert
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @return the converted amount
     */
    public double convert(final double amount, final String fromCurrency, final String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount; // Same currency
        }

        Double rate = getDirectRate(fromCurrency, toCurrency);
        // If Intermediate
        if (rate == null) {
            rate = findRateUsingIntermediate(fromCurrency, toCurrency);
        }

        return amount * rate;
    }

    /**
     * Retrieves the direct exchange rate between two currencies.
     *
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @return the exchange rate, or null if no direct rate exists
     */
    private Double getDirectRate(final String fromCurrency, final String toCurrency) {
        Map<String, Double> rates = EXCHANGE_RATE_MAP.get(fromCurrency);
        if (rates != null) {
            return rates.get(toCurrency);
        }
        return null;
    }

    /**
     * Finds an exchange rate between two currencies using intermediate currencies.
     *
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @return the exchange rate
     * @throws IllegalArgumentException if no conversion path is found
     */
    private double findRateUsingIntermediate(final String fromCurrency, final String toCurrency) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        Map<String, Double> rates = new HashMap<>();

        queue.add(fromCurrency);
        rates.put(fromCurrency, 1.0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentRate = rates.get(current);

            if (current.equals(toCurrency)) {
                return currentRate;
            }

            visited.add(current);

            // getOrDefault() for value or null
            for (Map.Entry<String, Double> entry : EXCHANGE_RATE_MAP
                    .getOrDefault(current, Collections.emptyMap()).entrySet()) {
                String neighbor = entry.getKey();
                double neighborRate = entry.getValue();

                if (!visited.contains(neighbor)) {
                    double cumulativeRate = currentRate * neighborRate;
                    rates.put(neighbor, cumulativeRate);
                    queue.add(neighbor);
                }
            }
        }

        throw new IllegalArgumentException("No conversion path found for "
                + fromCurrency + " -> " + toCurrency);
    }
}
