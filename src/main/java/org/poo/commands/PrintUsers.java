package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account.Account;
import org.poo.entities.Card.Card;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.services.AccountService;
import org.poo.services.CardService;
import org.poo.services.UserService;

import java.util.List;

/**
 * Command for printing users, their accounts and associated cards.
 * This class is designed to be used as is, and it is not intended for extension.
 */
public final class PrintUsers implements Command {
    private final UserService userService;
    private final AccountService accountService;
    private final CardService cardService;
    private final CommandInput command;
    private final ArrayNode output;

    public PrintUsers(final UserService userService,
                      final AccountService accountService,
                      final CardService cardService,
                      final CommandInput command,
                      final ArrayNode output) {
        this.userService = userService;
        this.accountService = accountService;
        this.cardService = cardService;
        this.command = command;
        this.output = output;
    }

    @Override
    public void execute() {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("command", "printUsers");
            outputNode.put("timestamp", command.getTimestamp());

            ArrayNode usersArray = outputNode.putArray("output");

            for (final User user : userService.getAllUsers()) {
                ObjectNode userNode = mapper.createObjectNode();
                List<Account> accounts = accountService.getAccounts(user.getEmail());
                ArrayNode accountsArray = userNode.putArray("accounts");

                for (final Account account : accounts) {
                    ObjectNode accountNode = mapper.createObjectNode();
                    accountNode.put("balance", account.getBalance());
                    accountNode.put("IBAN", account.getIban());
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getAccountType());

                    List<Card> cards = cardService.getCards(account.getIban());
                    ArrayNode cardsArray = accountNode.putArray("cards");

                    for (final Card card : cards) {
                        ObjectNode cardNode = mapper.createObjectNode();
                        cardNode.put("cardNumber", card.getNumber());
                        cardNode.put("status", card.getStatus().name());
                        cardsArray.add(cardNode);
                    }

                    accountsArray.add(accountNode);
                }

                userNode.put("firstName", user.getFirstName());
                userNode.put("lastName", user.getLastName());
                userNode.put("email", user.getEmail());
                usersArray.add(userNode);
            }

            output.add(outputNode);
    }

}
