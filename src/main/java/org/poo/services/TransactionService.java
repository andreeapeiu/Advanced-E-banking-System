package org.poo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.repository.AccountRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.fileio.CommandInput;

import java.util.List;

/**
 * Service for handling transactions.
 */
public final class TransactionService {
    private final TransactionRepository transactionRepository;

    /**
     * Constructor to initialize the TransactionService with a repository.
     *
     * @param transactionRepository the transaction repository.
     */
    public TransactionService(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Creates and saves a transaction.
     *
     * @param email            User email.
     * @param fromAccount      Sender account.
     * @param toAccount        Receiver account.
     * @param amount           Amount to transfer.
     * @param currency         Currency type.
     * @param timestamp        Timestamp of the transaction.
     * @param description      Description of the transaction.
     * @param userRepository   User repository for validation.
     * @param accountRepository Account repository for validation.
     * @param status           Status of the transaction.
     * @return the created Transaction.
     */
    public Transaction createTransaction(final String email, final String fromAccount,
                                         final String toAccount, final double amount,
                                         final String currency, final int timestamp,
                                         final String description,
                                         final UserRepository userRepository,
                                         final AccountRepository accountRepository,
                                         final TransactionStatus status) {

        Transaction transaction = new Transaction(email, fromAccount, toAccount,
                amount, currency, timestamp, description, status);
        transactionRepository.saveTransaction(transaction);
        return transaction;
    }

    /**
     * Gets all transactions for a specific account.
     *
     * @param account the account for which to retrieve transactions.
     * @return a list of transactions associated with the given account.
     */
    public List<Transaction> getTransactionsForAccount(final String account) {
        return transactionRepository.getTransactionsByAccount(account);
    }

    /**
     * Prints the transactions based on a given command.
     *
     * @param command          The input command.
     * @param output           The output array to store the result.
     * @param userRepository   User repository for user validation.
     */
    public void printTransactions(final CommandInput command, final ArrayNode output,
                                  final UserRepository userRepository) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.createObjectNode();
        transactionNode.put("command", "printTransactions");
        transactionNode.put("timestamp", command.getTimestamp());
        ArrayNode transactionsArray = transactionNode.putArray("output");

        for (Transaction transaction : transactionRepository.getAllTransactions()) {
            // Check if the transaction belongs to the user
            if (!transaction.getEmail().equals(command.getEmail())) {
                continue;
            }

            ObjectNode outputNode = mapper.createObjectNode();
            formatPrint(transaction, command, output, outputNode, userRepository);

            // Add the processed transaction to the output array
            transactionsArray.add(outputNode);
        }

        output.add(transactionNode);
    }

    /**
     * Formats a transaction for printing.
     *
     * @param transaction     The transaction to format.
     * @param command         The command input.
     * @param output          The output array to store the result.
     * @param outputNode      The output node to store the formatted transaction.
     * @param userRepository  The user repository for validation.
     */
    public static void formatPrint(final Transaction transaction, final CommandInput command,
                                   final ArrayNode output, final ObjectNode outputNode,
                                   final UserRepository userRepository) {
        outputNode.put("timestamp", transaction.getTimestamp());
        outputNode.put("description", transaction.getDescription());

        // Handle transactions based on their type
        switch (transaction.getType()) {
            case SEND_MONEY:
                outputNode.put("senderIBAN", transaction.getFromAccount());
                outputNode.put("receiverIBAN", transaction.getToAccount());
                outputNode.put("amount", transaction.getAmount() + " " + transaction.getCurrency());
                if (transaction.getStatus().equals(TransactionStatus.sent)) {
                    outputNode.put("transferType", "sent");
                } else {
                    outputNode.put("transferType", "received");
                }
                break;

            case ADD_FUNDS:
                outputNode.put("account", transaction.getFromAccount());
                outputNode.put("amount", transaction.getAmount() + " " + transaction.getCurrency());
                break;

            case CREATE_CARD:
                outputNode.put("account", transaction.getFromAccount());
                outputNode.put("cardHolder", transaction.getEmail());
                outputNode.put("card", transaction.getCardNumber());
                break;

            case PAY_ONLINE:
                outputNode.put("amount", transaction.getAmount());
                outputNode.put("commerciant", transaction.getCommerciant());
                break;

            case DELETE_ACCOUNT:
                outputNode.put("description", transaction.getDescription());
                outputNode.put("timestamp", transaction.getTimestamp());
                break;

            case DELETE_CARD:
                outputNode.put("account", transaction.getFromAccount());
                outputNode.put("card", transaction.getCardNumber());
                outputNode.put("cardHolder", transaction.getEmail());
                break;

            case INSUFFICIENT_FUNDS:
                break;

            case INSUFFICIENT_FUNDS_FOR_SPLIT:
                outputNode.put("amount", transaction.getAmount());
                outputNode.put("currency", transaction.getCurrency());
                outputNode.put("description", transaction.getDescription());
                outputNode.put("error", transaction.getError());
                outputNode.put("timestamp", transaction.getTimestamp());

                // Array for involved accounts
                ArrayNode involvedAccountsArray1 = outputNode.putArray("involvedAccounts");
                for (String account : transaction.getInvolvedAccounts()) {
                    involvedAccountsArray1.add(account);
                }
                break;

            case ADD_ACCOUNT:
                break;

            case CHECK_CARD_STATUS:
                break;

            case CHANGE_INTEREST_RATE:
                break;

            case SPLIT:
                outputNode.put("currency", transaction.getCurrency());
                outputNode.put("amount", transaction.getAmount());
                ArrayNode involvedAccountsArray = outputNode.putArray("involvedAccounts");
                for (String account : transaction.getInvolvedAccounts()) {
                    involvedAccountsArray.add(account);
                }
                break;

            default:
                outputNode.put("error", "Unknown transaction type");
                break;
        }
    }
}
