package org.poo.services.cashback;

import org.poo.entities.Account.Account;
import org.poo.entities.Commerciants.Commerciant;
import org.poo.entities.Transaction;
import org.poo.services.ExchangeService;

/**
 * Interface for cashback strategies.
 */
public interface CashbackStrategy {
    /**
     * Method for calculating cashback.
     *
     * @param account         The user's account.
     * @param commerciant     The merchant.
     * @param transaction     The current transaction.
     * @param exchangeService The currency exchange service.
     * @return The calculated cashback value.
     */
    double calculateCashback(Account account, Commerciant commerciant,
                             Transaction transaction,
                             ExchangeService exchangeService);
}
