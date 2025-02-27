package org.poo.services.cashback;

import java.util.HashMap;
import java.util.Map;
import org.poo.entities.Account.Account;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.entities.Transaction;
import org.poo.services.ExchangeService;

/**
 * Cashback strategy based on the total amount spent at merchants.
 */
public final class SpendingThresholdCashbackStrategy implements CashbackStrategy {

    // Spending thresholds (in RON)
    private static final double FIRST_THRESHOLD = 100.0;
    private static final double SECOND_THRESHOLD = 300.0;
    private static final double THIRD_THRESHOLD = 500.0;

    // Cashback rates (as percentages, e.g., 0.25 = 25%)
    private static final double THIRD_STANDARD_PERCENT = 0.25;
    private static final double THIRD_SILVER_PERCENT = 0.50;
    private static final double THIRD_GOLD_PERCENT = 0.70;

    private static final double SECOND_STANDARD_PERCENT = 0.20;
    private static final double SECOND_SILVER_PERCENT = 0.40;
    private static final double SECOND_GOLD_PERCENT = 0.60;

    private static final double FIRST_STANDARD_PERCENT = 0.10;
    private static final double FIRST_SILVER_PERCENT = 0.30;
    private static final double FIRST_GOLD_PERCENT = 0.50;

    // Used to convert from percentages to fractional values (e.g., 25% -> 0.25)
    private static final double PERCENT_DIVISOR = 100.0;

    /**
     * Calculates cashback based on the total amount spent by an account at different merchants.
     *
     * @param account         the user's account
     * @param commerciant     the merchant where the transaction is made
     * @param transaction     the transaction details
     * @param exchangeService the service used for currency conversion
     * @return the cashback value in the original transaction currency
     */
    @Override
    public double calculateCashback(
            final Account account,
            final Commerciant commerciant,
            final Transaction transaction,
            final ExchangeService exchangeService
    ) {

        Map<String, Double> moneySpentMap =
                account.getMoneySpentAtCommerciantsWithCashbackStrategyThreshold();

        if (moneySpentMap == null) {
            account.setMoneySpentAtCommerciantsWithCashbackStrategyThreshold(
                    new HashMap<>()
            );
            moneySpentMap = account.getMoneySpentAtCommerciantsWithCashbackStrategyThreshold();
        }

        final double convertedAmountInRon = exchangeService.convert(
                transaction.getAmount(),
                account.getCurrency(),
                "RON"
        );

        final String merchantName = commerciant.getName();
        final double oldValue = moneySpentMap.getOrDefault(merchantName, 0.0);
        final double newValue = oldValue + convertedAmountInRon;
        moneySpentMap.put(merchantName, newValue);

        final double totalSpent = moneySpentMap
                .values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        final String userType = account.getOwner().getPlan();
        double cashback = 0.0;

        if (totalSpent >= THIRD_THRESHOLD) {
            cashback = calculateCashbackByPlan(
                    userType,
                    transaction.getAmount(),
                    THIRD_STANDARD_PERCENT,
                    THIRD_SILVER_PERCENT,
                    THIRD_GOLD_PERCENT
            );
        } else if (totalSpent >= SECOND_THRESHOLD) {
            cashback = calculateCashbackByPlan(
                    userType,
                    transaction.getAmount(),
                    SECOND_STANDARD_PERCENT,
                    SECOND_SILVER_PERCENT,
                    SECOND_GOLD_PERCENT
            );
        } else if (totalSpent >= FIRST_THRESHOLD) {
            cashback = calculateCashbackByPlan(
                    userType,
                    transaction.getAmount(),
                    FIRST_STANDARD_PERCENT,
                    FIRST_SILVER_PERCENT,
                    FIRST_GOLD_PERCENT
            );
        }

        return cashback;
    }

    /**
     * Helper method to determine cashback based on the user's plan
     * (Standard/Student, Silver, Gold) and the corresponding percentages.
     *
     * @param userType        the user's plan
     * @param amount          the transaction amount
     * @param standardPercent the cashback percentage for Standard/Student plan
     * @param silverPercent   the cashback percentage for Silver plan
     * @param goldPercent     the cashback percentage for Gold plan
     * @return the cashback value in the original transaction currency
     */
    private double calculateCashbackByPlan(
            final String userType,
            final double amount,
            final double standardPercent,
            final double silverPercent,
            final double goldPercent
    ) {
        switch (userType.toLowerCase()) {
            case "standard":
            case "student":
                return (standardPercent / PERCENT_DIVISOR) * amount;
            case "silver":
                return (silverPercent / PERCENT_DIVISOR) * amount;
            case "gold":
                return (goldPercent / PERCENT_DIVISOR) * amount;
            default:
                return 0.0;
        }
    }
}
