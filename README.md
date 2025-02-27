# Project Assignment POO - J. POO Morgan - Phase Two

## Project Overview

This project extends the functionality of an e-banking system developed in Phase One. The second
phase focuses on enhancing the user experience by adding cashback strategies, business
accounts, savings withdrawals, custom split payments, and detailed 
reporting capabilities. The system is designed
to simulate basic banking operations using OOP concepts such as design patterns. The system
focuses on modular design, security, and scalability, ensuring a smooth and reliable workflow.

Users can perform various financial operations through commands designed to simulate real-world
transactions, offering both flexibility and precision. Some of these commands will be detailed
in the following sections.

## Objectives

The implemented system will enable the following:

- Creation, management, and deletion of bank accounts and associated cards.
- Execution of financial transactions such as deposits, withdrawals, bank transfers, and
  online payments.
- Generation of transaction reports for users and commerciants.
- Currency exchange management and support for multiple currencies.

## Running the Banking System

Clone the repository: [GitHub Link](#)  
(**https://github.com/andreeapeiu/Advanced-E-banking-System**).  
Execute `Main.java` to start the banking simulation.

## Project Structure

The project is organized into several packages that group classes based on their functionalities.

- **commands**: Contains classes for each command supported by the system. These are managed by
  the `CommandFactory` class for instantiating the required commands.
- **entities**: Contains classes representing the main objects in the system: `User`, `Account`,
  `Card`, `Transaction` etc. Accounts and cards are organized using **Inheritance**.
    - `Account`: Abstract parent class for accounts, extended by `ClassicAccount` and
      `SavingsAccount`.
    - `Card`: Parent class for cards, extended by `StandardCard` and `OneTimeCard`.
- **services**: Implements additional methods for managing entities. Example: `AccountService`
  for account-related operations.
- **repository**: Implements storage for each type of entity. (Things to improve: could be
  Singleton)
- **fileio**: Contains classes for parsing input JSON files.
- **utils**: Utility class for common operations such as IBAN generation.

## Design Patterns Used

1. Factory Pattern:
    - Used in `CommandFactory` to dynamically create command objects based on input.

2. Inheritance:
    - `ClassicAccount` and `SavingsAccount` extend the abstract `Account` class.
    - Similarly, `OneTimeCard` and `StandardCard` extend the `Card` class.

3. Singleton Pattern:
    - Used in `CurrencyExchange` to ensure a single instance for currency conversion.
    - Should be used for classes in the `repository` package to ensure each entity is managed
      through a single instance of its repository. (Further improvements)

4. Command Pattern:
    - Transforms the command into an object

5. Strategy Pattern:
    - At run time it will be chosen what cashback strategy does the commerciant uses and
    applies the one needed.


## Execution Flow

1. The input file is processed by classes in the `fileio` package to initialize the required
   entities.
2. Each command is handled by `CommandExecutor`, which uses `CommandFactory` to create the
   appropriate instance.
3. The command is executed by calling the `execute` method, which performs the required
   operations.
4. Results are collected and written to the output JSON file.

## Account Management Commands

- **Add Account**: Creates a new bank account for a user. Supports business, savings and classic
  account types. Includes initial setup for currency and optional interest rate.

- **Delete Account**: Removes an account if the balance is zero. Associated cards are also
  deleted.

- **Set Minimum Balance**: Defines a minimum balance for an account, triggering warnings or
  freezing cards if violated.

- **Cash withdrawal**: Allows users to withdraw money from their accounts. The payment is in RON.

- **Business Account**: Creates a business account with a deposit and spending limit.

- **Split Payment**: Divides a specified amount among multiple accounts. Supports custom and equal
  sums for each account.

- **Cashback Strategy**: Sets a cashback strategy for a business account. The strategy can be
  based on the total amount spent or the number of transactions.

# Cashback Overview

The cashback system rewards users for their transactions based on two strategies: 
**Number of Transactions** and **Spending Thresholds**. These strategies encourage user
engagement and provide tangible benefits for frequent usage.

## Cashback Strategies

### `Number of Transactions`
Rewards are granted when users reach specific transaction milestones:
- **2 transactions**: 2% cashback.
- **5 transactions**: 5% cashback.
- **10 transactions**: 10% cashback.

Discounts apply across categories such as:
- **Food**
- **Clothes**
- **Tech**

Once earned, discounts can be used for any merchant within the respective category.

### `Spending Thresholds`
Rewards are based on cumulative spending in RON, with higher percentages for higher thresholds:
- **100 RON**:
    - Standard/Student: 0.1%
    - Silver: 0.3%
    - Gold: 0.5%
- **300 RON**:
    - Standard/Student: 0.2%
    - Silver: 0.4%
    - Gold: 0.55%
- **500 RON**:
    - Standard/Student: 0.25%
    - Silver: 0.5%
    - Gold: 0.7%

All spending is converted to RON, and cashback is applied when thresholds are met.

## Key Features
- Cashback is calculated per account.
- Rewards vary based on user plans (Standard, Student, Silver, Gold).
- Encourages engagement by rewarding frequent usage or high spending.

This system enhances user satisfaction by providing dynamic rewards tailored 
to individual transaction patterns.


## Card Management Commands

- **Create Card**: Generates a standard card linked to an account.

- **Create One-Time Card**: Issues a single-use card for secure transactions. Automatically
  regenerates after use.

- **Delete Card**: Removes a card permanently.

- **Check Card Status**: Verifies the status of a card (active, frozen).

## Transaction Commands

- **Add Funds**: Deposits money into a specified account.

- **Pay Online**: Deducts funds from a card for an online payment, supporting multiple currencies.  
  If the card is one-time, it is automatically regenerated.

- **Send Money**: Transfers funds between accounts with automatic currency conversion.

- **Split Payment**: Divides a specified amount among multiple accounts equally.

## Reporting Commands

- **Print Users**: Outputs all user details along with associated accounts and cards.

- **Print Transactions**: Lists all transactions for a specific user. Verifies if the email
  and iban are associated.

- **Report**: Generates a detailed report of account balances and transactions.

- **Spendings Report**: Lists all transactions for a specific account within a specified
  time frame.

- **businessReport**: Generates a report for business accounts, including the total amount
  spent and the remaining balance.

## Future Improvements

- Implement Singleton pattern for repository classes.
- Use inheritance better for entities.
