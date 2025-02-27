package org.poo.services.cashback;

import java.util.HashMap;
import java.util.Map;

import org.poo.entities.Account.Account;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.entities.Transaction;
import org.poo.services.ExchangeService;

/**
 * A cashback strategy that takes into account the number of transactions
 * made at each merchant.
 */
public final class NrOfTransactionsCashbackStrategy implements CashbackStrategy {

    private static final String FOOD_KEY = "Food";
    private static final String CLOTHES_KEY = "Clothes";
    private static final String TECH_KEY = "Tech";

    private static final int FOOD_TRANSACTIONS_THRESHOLD = 2;
    private static final double FOOD_CASHBACK_RATE = 0.02;

    private static final int CLOTHES_TRANSACTIONS_THRESHOLD = 5;
    private static final double CLOTHES_CASHBACK_RATE = 0.05;

    private static final int TECH_TRANSACTIONS_THRESHOLD = 10;
    private static final double TECH_CASHBACK_RATE = 0.1;

    private static final double NO_CASHBACK = 0.0;

    private final Map<String, Map<Integer, Double>> cashbackRules;

    /**
     * Initializes the cashback rules based on the number of transactions
     * made at certain merchants.
     */
    public NrOfTransactionsCashbackStrategy() {
        cashbackRules = new HashMap<>();

        cashbackRules.put(FOOD_KEY,
                Map.of(FOOD_TRANSACTIONS_THRESHOLD, FOOD_CASHBACK_RATE));
        cashbackRules.put(CLOTHES_KEY,
                Map.of(CLOTHES_TRANSACTIONS_THRESHOLD, CLOTHES_CASHBACK_RATE));
        cashbackRules.put(TECH_KEY,
                Map.of(TECH_TRANSACTIONS_THRESHOLD, TECH_CASHBACK_RATE));
    }

    /**
     * Calculates the cashback amount for a transaction based on rules
     * defined by the number of transactions made at a given merchant.
     *
     * @param account         the account from which the transaction is made
     * @param commerciant     the merchant to whom the payment is made
     * @param transaction     the transaction details (amount)
     * @param exchangeService the currency exchange service (if needed)
     * @return the calculated cashback amount
     */
    @Override
    public double calculateCashback(
            final Account account,
            final Commerciant commerciant,
            final Transaction transaction,
            final ExchangeService exchangeService
    ) {

        if (account.getNrOfTransactionsPerCommerciants() == null) {
            account.setNrOfTransactionsPerCommerciants(new HashMap<>());
        }

        final String merchantName = commerciant.getName();
        account.getNrOfTransactionsPerCommerciants()
                .putIfAbsent(merchantName, 0);

        final int nrOfTransactions = account.getNrOfTransactionsPerCommerciants()
                .get(merchantName) + 1;

        account.getNrOfTransactionsPerCommerciants()
                .put(merchantName, nrOfTransactions);

        final Map<Integer, Double> rules = cashbackRules.get(commerciant.getType());
        if (rules != null && rules.containsKey(nrOfTransactions)) {
            return rules.get(nrOfTransactions) * transaction.getAmount();
        }

        return NO_CASHBACK;
    }
}
