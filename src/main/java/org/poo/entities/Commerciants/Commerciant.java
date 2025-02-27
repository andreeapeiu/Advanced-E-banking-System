package org.poo.entities.Commerciants;

/**
 * Entity class representing a commerciant in the system.
 * A commerciant has an ID, name, account details, type, and cashback strategy.
 */
public class Commerciant {

    private int id;
    private String name;
    private String account;
    private String type;
    private CashbackType cashbackStrategy;

    /**
     * Default constructor.
     */
    public Commerciant() {
    }

    public Commerciant(final String name, final CashbackType cashbackStrategy, final String type) {
        this.name = name;
        this.cashbackStrategy = cashbackStrategy;
        this.type = type;
        this.id = 0;
        this.account = "";
    }

    /**
     * Parameterized constructor.
     *
     * @param id the ID of the commerciant.
     * @param name the name of the commerciant.
     * @param account the account details of the commerciant.
     * @param type the type of the commerciant (e.g., Tech, Clothes).
     * @param cashbackStrategy the cashback strategy used by the commerciant.
     */
    public Commerciant(final int id, final String name, final String account,
                       final String type, final CashbackType cashbackStrategy) {
        this.id = id;
        this.name = name;
        this.account = account;
        this.type = type;
        this.cashbackStrategy = cashbackStrategy;
    }

    /**
     * Gets the ID of the commerciant.
     *
     * @return the commerciant's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the commerciant.
     *
     * @param id the ID to set.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Gets the name of the commerciant.
     *
     * @return the commerciant's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the commerciant.
     *
     * @param name the name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the account details of the commerciant.
     *
     * @return the commerciant's account.
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the account details of the commerciant.
     *
     * @param account the account to set.
     */
    public void setAccount(final String account) {
        this.account = account;
    }

    /**
     * Gets the type of the commerciant.
     *
     * @return the commerciant's type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the commerciant.
     *
     * @param type the type to set.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the cashback strategy of the commerciant.
     *
     * @return the cashback strategy.
     */
    public CashbackType getCashbackStrategy() {
        return cashbackStrategy;
    }

    /**
     * Sets the cashback strategy of the commerciant.
     *
     * @param cashbackStrategy the strategy to set.
     */
    public void setCashbackStrategy(final CashbackType cashbackStrategy) {
        this.cashbackStrategy = cashbackStrategy;
    }
}
