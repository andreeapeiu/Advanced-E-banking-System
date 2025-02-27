package org.poo.services.cashback;

import org.poo.entities.Account.Account;
import org.poo.entities.Commerciants.CashbackType;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.entities.Transaction;
import org.poo.repository.AccountRepository;
import org.poo.services.CommerciantsService;
import org.poo.services.ExchangeService;

import java.util.EnumMap;
import java.util.Map;

/**
 * Service class responsible for calculating cashback.
 */
public class CashbackCalculator {
    private final Map<CashbackType, CashbackStrategy> strategyMap;
    private final CommerciantsService commerciantsService;
    private final ExchangeService exchangeService;
    private final AccountRepository accountRepository;

    /**
     * Constructor for initializing the CashbackCalculator with required services.
     *
     * @param commerciantsService the service for managing merchants.
     * @param exchangeService     the service for handling currency exchange.
     * @param accountRepository   the repository for managing user accounts.
     */
    public CashbackCalculator(final CommerciantsService commerciantsService,
                              final ExchangeService exchangeService,
                              final AccountRepository accountRepository) {
        this.strategyMap = new EnumMap<>(CashbackType.class);
        strategyMap.put(CashbackType.nrOfTransactions, new NrOfTransactionsCashbackStrategy());
        strategyMap.put(CashbackType.spendingThreshold, new SpendingThresholdCashbackStrategy());

        this.commerciantsService = commerciantsService;
        this.exchangeService = exchangeService;
        this.accountRepository = accountRepository;
    }

    /**
     * Main cashback calculation method.
     *
     * @param accountIban        IBAN of the user's account.
     * @param transaction        The current transaction.
     * @param commerciantId      The merchant's ID.
     * @param accountRepository  The account repository.
     * @param commerciantsService The service for merchants.
     * @param exchangeService    The currency exchange service.
     * @return The calculated cashback value.
     */
    public double calculateCashback(
            final String accountIban,
            final Transaction transaction,
            final int commerciantId,
            final AccountRepository accountRepository,
            final CommerciantsService commerciantsService,
            final ExchangeService exchangeService) {

        Account account = accountRepository.getAccountByIban(accountIban);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        // Find the merchant
        Commerciant commerciant = commerciantsService.getCommerciantById(commerciantId);
        if (commerciant == null) {
            throw new IllegalArgumentException("Merchant not found");
        }

        // Identify the cashback strategy
        CashbackType cashbackType = commerciant.getCashbackStrategy();
        CashbackStrategy strategy = strategyMap.get(cashbackType);
        if (strategy == null) {
            throw new UnsupportedOperationException("Unsupported cashback strategy: "
                    + cashbackType);
        }

        // Calculate cashback using the identified strategy
        return strategy.calculateCashback(account, commerciant, transaction, exchangeService);
    }
}
