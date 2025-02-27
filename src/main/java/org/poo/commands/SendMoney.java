package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;
import org.poo.entities.Card.CardStatus;
import org.poo.entities.Transaction;
import org.poo.entities.TransactionStatus;
import org.poo.entities.TransactionType;
import org.poo.entities.accountAlias.Alias;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.AliasRepository;
import org.poo.repository.TransactionRepository;
import org.poo.services.ExchangeService;

/**
 * Command for sending money between accounts.
 */
public final class SendMoney implements Command {
    private static final double STANDARD_COMMISSION_RATE = 0.2 / 100;
    private static final double SILVER_COMMISSION_RATE = 0.1 / 100;
    private static final double SILVER_COMMISSION_THRESHOLD = 500;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AliasRepository aliasRepository;
    private final CommandInput command;
    private final ArrayNode output;

    public SendMoney(final AccountRepository accountRepository,
                     final TransactionRepository transactionRepository,
                     final AliasRepository aliasRepository,
                     final CommandInput command,
                     final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.aliasRepository = aliasRepository;
        this.command = command;
        this.output = output;
    }

    @Override
    public void execute() {
        // Find the sender account by IBAN or alias
        Account sender = accountRepository.findAccountByIban(command.getAccount());
        if (sender == null) {
            Alias senderAlias = aliasRepository.findAliasByAlias(command.getAccount());
            if (senderAlias != null) {
                sender = accountRepository.findAccountByIban(senderAlias.getAccountIBAN());
            }
        }

        if (sender == null) {
            createErrorOutput("User not found", command.getTimestamp());
            return;
        }

        Account receiver = accountRepository.findAccountByIban(command.getReceiver());
        if (receiver == null) {
            Alias receiverAlias = aliasRepository.findAliasByAlias(command.getReceiver());
            if (receiverAlias != null) {
                receiver = accountRepository.findAccountByIban(receiverAlias.getAccountIBAN());
            }
        }

        if (receiver == null) {
            createErrorOutput("User not found", command.getTimestamp());
            return;
        }

        Card card = sender.getCard(command.getAccount());
        double commission = 0.0;

        if (card != null && card.getStatus().equals(CardStatus.frozen)) {
            // Create a transaction if the card is frozen
            Transaction transaction = new Transaction(
                    sender.getEmail(),
                    sender.getIban(),
                    receiver.getIban(),
                    0.0,
                    sender.getCurrency(),
                    command.getTimestamp(),
                    "The card is frozen",
                    TransactionType.CHECK_CARD_STATUS,
                    null,
                    null,
                    TransactionStatus.sent
            );
            transactionRepository.recordTransaction(transaction);
            return;
        }

        // Get sender's and receiver's currencies
        String senderCurrency = sender.getCurrency();
        String receiverCurrency = receiver.getCurrency();

        // Check if the sender has sufficient funds
        if (sender.getBalance() - sender.getBlockAmount() < command.getAmount()) {
            Transaction transaction = new Transaction(
                    sender.getEmail(), sender.getIban(), receiver.getIban(),
                    0.0, senderCurrency, command.getTimestamp(),
                    "Insufficient funds", TransactionType.INSUFFICIENT_FUNDS,
                    null, null, TransactionStatus.sent);
            transactionRepository.recordTransaction(transaction);
            return;
        }

        if (senderCurrency.equals(receiverCurrency)) {
            sender.setBalance(sender.getBalance() - command.getAmount());
            receiver.setBalance(receiver.getBalance() + command.getAmount());

            ExchangeService exchangeService = ExchangeService.getInstance();
            double suminron = exchangeService.convert(command.getAmount(), senderCurrency, "RON");

            if (sender.getOwner().getPlan().equals("standard")) {
                commission = STANDARD_COMMISSION_RATE * command.getAmount();
            } else if (sender.getOwner().getPlan().equals("silver")) {
                if (suminron >= SILVER_COMMISSION_THRESHOLD) {
                    commission = SILVER_COMMISSION_RATE * command.getAmount();
                }
            }

            sender.setBalance(sender.getBalance() - commission);

            // Create the transaction for sender
            Transaction transaction = new Transaction(
                    sender.getEmail(), sender.getIban(), receiver.getIban(),
                    command.getAmount(), senderCurrency, command.getTimestamp(),
                    command.getDescription(), TransactionType.SEND_MONEY,
                    null, null, TransactionStatus.sent);
            transactionRepository.recordTransaction(transaction);

            // Create transaction for receiver
            Transaction received = new Transaction(
                    receiver.getEmail(), sender.getIban(), receiver.getIban(),
                    command.getAmount(), receiverCurrency, command.getTimestamp(),
                    command.getDescription(), TransactionType.SEND_MONEY, null,
                    null, TransactionStatus.received);
            transactionRepository.recordTransaction(received);

        } else {
            // Convert the amount if currencies do not match
            ExchangeService exchangeService = ExchangeService.getInstance();
            double convertedAmount = exchangeService.convert(command.getAmount(),
                    senderCurrency, receiverCurrency);

            sender.setBalance(sender.getBalance() - command.getAmount());
            receiver.setBalance(receiver.getBalance() + convertedAmount);

            double suminron = exchangeService.convert(command.getAmount(), senderCurrency, "RON");

            if (sender.getOwner().getPlan().equals("standard")) {
                commission = STANDARD_COMMISSION_RATE * command.getAmount();
            } else if (sender.getOwner().getPlan().equals("silver")) {
                if (suminron >= SILVER_COMMISSION_THRESHOLD) {
                    commission = SILVER_COMMISSION_RATE * command.getAmount();
                }
            }

            sender.setBalance(sender.getBalance() - commission);

            // Create the transaction
            Transaction transaction = new Transaction(
                    sender.getEmail(), sender.getIban(), receiver.getIban(),
                    command.getAmount(), senderCurrency, command.getTimestamp(),
                    command.getDescription(), TransactionType.SEND_MONEY,
                    null, null, TransactionStatus.sent);
            transactionRepository.recordTransaction(transaction);

            // Create transaction for receiver after currency conversion
            Transaction received = new Transaction(
                    receiver.getEmail(),
                    sender.getIban(),
                    receiver.getIban(),
                    convertedAmount,
                    receiverCurrency,
                    command.getTimestamp(),
                    command.getDescription(),
                    TransactionType.SEND_MONEY,
                    null,
                    null,
                    TransactionStatus.received
            );
            transactionRepository.recordTransaction(received);
        }
    }

    private void createErrorOutput(final String description, final int timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", "sendMoney");
        ObjectNode out2 = mapper.createObjectNode();
        out2.put("description", description);
        out.put("timestamp", timestamp);
        out2.put("timestamp", timestamp);
        out.set("output", out2);
        output.add(out);
    }
}
