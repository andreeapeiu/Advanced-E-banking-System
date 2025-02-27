package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.fileio.CommandInput;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.poo.entities.TransactionType.INSUFFICIENT_FUNDS_FOR_SPLIT_CUSTOM;

/**
 * Command for printing transactions of a user.
 */
public final class PrintTransactions implements Command {
    private final TransactionRepository transactionRepository;
    private final CommandInput command;
    private final ArrayNode output;
    private final UserRepository userRepository;

    public PrintTransactions(final TransactionRepository transactionRepository,
                             final CommandInput command,
                             final ArrayNode output,
                             final UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.command = command;
        this.output = output;
        this.userRepository = userRepository;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.createObjectNode();
        transactionNode.put("command", "printTransactions");
        transactionNode.put("timestamp", command.getTimestamp());
        ArrayNode transactionsArray = transactionNode.putArray("output");

        List<Transaction> transactions = transactionRepository.getAllTransactions();
        List<Transaction> userTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.getEmail().equals(command.getEmail())) {
                userTransactions.add(transaction);
            }
        }

        for (int i = 0; i < userTransactions.size() - 1; i++) {
            for (int j = 0; j < userTransactions.size() - i - 1; j++) {
                if (userTransactions.get(j).getTimestamp()
                        > userTransactions.get(j + 1).getTimestamp()) {
                    Transaction temp = userTransactions.get(j);
                    userTransactions.set(j, userTransactions.get(j + 1));
                    userTransactions.set(j + 1, temp);
                }
            }
        }

        for (final Transaction transaction : userTransactions) {
            // If not all accepted the split payment, don't show the transaction
            if ((transaction.getType().equals(TransactionType.SPLIT_CUSTOM)
                    || transaction.getType().equals(TransactionType.SPLIT))
                    && transaction.getStatus().equals(TransactionStatus.PENDING)) {
                continue;
            }

            if ((transaction.getType().equals(TransactionType.INSUFFICIENT_FUNDS_FOR_SPLIT_CUSTOM)
                    || transaction.getType().equals(TransactionType.INSUFFICIENT_FUNDS_FOR_SPLIT))
                    && transaction.getStatus().equals(TransactionStatus.PENDING)) {
                continue;
            }

            if (transaction.getType().equals(TransactionType.DEPOSIT)
                    || transaction.getType().equals(TransactionType.SPENDING)) {
                continue;
            }

            ObjectNode outputNode = mapper.createObjectNode();
            formatTransaction(transaction, outputNode);

            transactionsArray.add(outputNode);
        }

        output.add(transactionNode);
    }


    private void formatTransaction(final Transaction transaction,
                                   final ObjectNode outputNode) {
        if (!transaction.getType().equals(TransactionType.SPLIT_CUSTOM)
                && !transaction.getType().equals(TransactionType.SPLIT)
                && !transaction.getType().equals(INSUFFICIENT_FUNDS_FOR_SPLIT_CUSTOM)
                && !transaction.getType().equals(TransactionType.INSUFFICIENT_FUNDS_FOR_SPLIT)
                && !transaction.getType().equals(TransactionType.DEPOSIT)) {

            outputNode.put("timestamp", transaction.getTimestamp());
            outputNode.put("description", transaction.getDescription());
        }

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
                outputNode.put("amount", transaction.getAmount()
                        + " " + transaction.getCurrency());
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
                if (transaction.getStatus().equals(TransactionStatus.successful)) {

                    outputNode.put("amount", transaction.getAmount());
                    outputNode.put("currency", transaction.getCurrency());
                    outputNode.put("description", transaction.getDescription());
                    outputNode.put("error", transaction.getError());
                    outputNode.put("timestamp", transaction.getTimestamp());
                    outputNode.put("splitPaymentType", "equal");
                    ArrayNode involvedAccountsArray1 = outputNode.putArray("involvedAccounts");
                    for (final String account : transaction.getInvolvedAccounts()) {
                        involvedAccountsArray1.add(account);
                    }
                }
                break;

            case ADD_ACCOUNT:
                break;

            case CHECK_CARD_STATUS:
                break;

            case CHANGE_INTEREST_RATE:
                break;

            case SPLIT:
                if (transaction.getStatus().equals(TransactionStatus.successful)) {

                    outputNode.put("currency", transaction.getCurrency());
                    outputNode.put("splitPaymentType", "equal");
                    outputNode.put("amount", transaction.getAmount());
                    ArrayNode involvedAccountsArray = outputNode.putArray("involvedAccounts");
                    for (final String account : transaction.getInvolvedAccounts()) {
                        involvedAccountsArray.add(account);
                    }
                    outputNode.put("timestamp", transaction.getTimestamp());
                    outputNode.put("description", transaction.getDescription());
                    if (!transaction.getError().equals("nesetat")) {
                        outputNode.put("error", transaction.getError());
                    }
                }
                break;

            case WITHDRAW_SAVINGS:
                outputNode.put("description", transaction.getDescription());
                outputNode.put("timestamp", transaction.getTimestamp());
                break;

            case UPGRADE_PLAN:
                outputNode.put("description", transaction.getDescription());
                outputNode.put("accountIBAN", transaction.getFromAccount());
                outputNode.put("newPlanType", transaction.getCommerciant());
                outputNode.put("timestamp", transaction.getTimestamp());
                break;

            case CASHWITHDRAWAL:
                if (transaction.getStatus().equals(TransactionStatus.successful)) {
                    outputNode.put("amount", transaction.getAmount());
                }
                break;

            case ADD_INTEREST:
                outputNode.put("amount", transaction.getAmount());
                outputNode.put("currency", transaction.getCurrency());
                break;

            case SPLIT_CUSTOM:
                if (transaction.getStatus().equals(TransactionStatus.successful)) {
                    outputNode.put("description", transaction.getDescription());
                    outputNode.put("currency", transaction.getCurrency());
                    outputNode.put("timestamp", transaction.getTimestamp());
                    outputNode.put("splitPaymentType", transaction.getSplitPaymentType());

                    if (!transaction.getError().equals("nesetat")) {
                        outputNode.put("error", transaction.getError());
                    }

                    if (transaction.getSplitPaymentType().equals("custom")) {
                        ArrayNode amountForUsersArray = outputNode.putArray("amountForUsers");
                        for (Double amount : transaction.getAmountForUsers()) {
                            amountForUsersArray.add(amount);
                        }
                    } else {
                        outputNode.put("amount", transaction.getAmount());
                    }

                    ArrayNode involvedAccountsArray2 = outputNode.putArray("involvedAccounts");
                    for (String account : transaction.getInvolvedAccounts()) {
                        involvedAccountsArray2.add(account);
                    }

                }
                break;

            case DEPOSIT:
                break;

            case SPENDING:
                break;

            case UPGRADE_PLAN_NO_FUNDS:
                break;

            default:
                outputNode.put("error", "Unknown transaction type");
                break;
        }
    }
}
