package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commands.Command;
import org.poo.commands.CommandFactory;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.fileio.CommerciantInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.AliasRepository;
import org.poo.repository.CommerciantsRepository;
import org.poo.repository.SpendingsRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.CardRepository;
import org.poo.services.AccountService;
import org.poo.services.AliasService;
import org.poo.services.CardService;
import org.poo.services.CommerciantsService;
import org.poo.services.TransactionService;
import org.poo.services.UserService;
import org.poo.services.ExchangeService;
import org.poo.utils.Utils;

public class CommandExecutor {
    private final ObjectInput inputData;
    private final ArrayNode output;
    private final CommandFactory commandFactory;

    private UserService userService;
    private AccountService accountService;
    private CardService cardService;
    private AliasService aliasService;
    private TransactionService transactionService;
    private CommerciantsService commerciantsService;

    /**
     * Main constructor of CommandExecutor class.
     *
     * @param inputData Data input.
     * @param output    Command execution results.
     * @param commandFactory Command factory.
     * @param userRepository User repository.
     * @param accountRepository Account repository.
     * @param transactionRepository Transaction repository.
     * @param cardRepository Card repository.
     * @param aliasRepository Alias repository.
     * @param spendingsRepository Spendings repository.
     * @param commerciantsRepository Commerciants repository.
     */
    public CommandExecutor(final ObjectInput inputData, final ArrayNode output,
                           final CommandFactory commandFactory,
                           final UserRepository userRepository,
                           final AccountRepository accountRepository,
                           final TransactionRepository transactionRepository,
                           final CardRepository cardRepository,
                           final AliasRepository aliasRepository,
                           final SpendingsRepository spendingsRepository,
                           final CommerciantsRepository commerciantsRepository) {
        this.inputData = inputData;
        this.output = output;
        this.commandFactory = commandFactory;

        // Initialize services using the provided repositories
        this.userService = new UserService(userRepository);
        this.accountService = new AccountService(accountRepository,
                transactionRepository, userRepository,
                cardRepository, aliasRepository);
        this.cardService = new CardService(accountRepository, cardRepository, userRepository,
                transactionRepository, spendingsRepository);
        this.aliasService = new AliasService(aliasRepository);
        this.transactionService = new TransactionService(transactionRepository);
        this.commerciantsService = new CommerciantsService(commerciantsRepository);

        // Configure CommandFactory with services
        commandFactory.setServices(userService, accountService, cardService, aliasService,
                transactionService, commerciantsService);

        initializeUsers();
        initializeCommerciants();
        initializeExchangeRates();
    }

    /**
     * Initializes all required services and repositories.
     */
    private void initializeServices() {
        // Initialize repositories
        final UserRepository userRepository = new UserRepository();
        final AccountRepository accountRepository = new AccountRepository();
        final TransactionRepository transactionRepository = new TransactionRepository();
        final CardRepository cardRepository = new CardRepository();
        final AliasRepository aliasRepository = new AliasRepository();
        final SpendingsRepository spendingsRepository = new SpendingsRepository();

        // Initialize services with repositories
        this.userService = new UserService(userRepository);
        this.accountService = new AccountService(accountRepository, transactionRepository,
                userRepository, cardRepository, aliasRepository);
        this.cardService = new CardService(accountRepository, cardRepository, userRepository,
                transactionRepository, spendingsRepository);
        this.aliasService = new AliasService(aliasRepository);
        this.transactionService = new TransactionService(transactionRepository);
    }

    /**
     * Adds users from input data to the system.
     */
    private void initializeUsers() {
        for (final UserInput userInput : inputData.getUsers()) {
            userService.addUser(userInput.getFirstName(),
                    userInput.getLastName(),
                    userInput.getEmail(),
                    userInput.getBirthDate(),
                    userInput.getOccupation());
            Utils.resetRandom();
        }
    }

    /**
     * Adds commerciants from input data to the system.
     */
    private void initializeCommerciants() {
        for (final CommerciantInput commerciantInput : inputData.getCommerciants()) {
            commerciantsService.addCommerciant(commerciantInput.getCommerciant(),
                    commerciantInput.getId(),
                    commerciantInput.getAccount(),
                    commerciantInput.getType(),
                    commerciantInput.getCashbackStrategy());
        }
    }

    /**
     * Adds exchange rates from input data to the system.
     */
    private void initializeExchangeRates() {
        ExchangeService.getInstance().resetInstance();

        if (inputData.getExchangeRates() != null) {
            for (final ExchangeInput exchangeInput : inputData.getExchangeRates()) {
                ExchangeService.getInstance().addExchangeRate(exchangeInput.getFrom(),
                        exchangeInput.getTo(), exchangeInput.getRate());
            }
        }
    }

    /**
     * Executes all commands from the input data.
     */
    public void executeCommands() {
        for (final var commandInput : inputData.getCommands()) {
            try {
                final Command command = commandFactory.createCommand(commandInput);
                command.execute();
            } catch (final Exception e) {
                output.add(createErrorOutput(commandInput.getCommand(), e.getMessage()));
            }
        }
    }

    /**
     * Creates an error output JSON object for failed commands.
     *
     * @param command The command name.
     * @param message The error message.
     * @return A JSON object containing error details.
     */
    private ObjectNode createErrorOutput(final String command, final String message) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("command", command);
        errorNode.put("error", message);
        return errorNode;
    }
}
