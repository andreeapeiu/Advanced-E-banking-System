package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.repository.AccountRepository;
import org.poo.repository.AliasRepository;
import org.poo.repository.CardRepository;
import org.poo.repository.TransactionRepository;
import org.poo.repository.UserRepository;
import org.poo.repository.SpendingsRepository;
import org.poo.repository.CommerciantsRepository;
import org.poo.repository.SplitsRepository;
import org.poo.services.CardService;
import org.poo.services.ExchangeService;
import org.poo.services.CommerciantsService;
import org.poo.services.TransactionService;
import org.poo.services.AccountService;
import org.poo.services.AliasService;

import org.poo.services.UserService;
import org.poo.services.cashback.CashbackCalculator;

public class CommandFactory {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AliasRepository aliasRepository;
    private final UserRepository userRepository;
    private final SpendingsRepository spendingsRepository;
    private final CommerciantsRepository commerciantsRepository;
    private final SplitsRepository splitsRepository;

    private UserService userService;
    private AccountService accountService;
    private CardService cardService;
    private AliasService aliasService;
    private CommerciantsService commerciantsService;
    private TransactionService transactionService;
    private CashbackCalculator cashbackCalculator;

    private final ArrayNode output;

    public CommandFactory(final AccountRepository accountRepository,
                          final TransactionRepository transactionRepository,
                          final CardRepository cardRepository,
                          final AliasRepository aliasRepository,
                          final UserRepository userRepository,
                          final SpendingsRepository spendingsRepository,
                          final CommerciantsRepository commerciantsRepository,
                          final SplitsRepository splitsRepository,
                          final ArrayNode output) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.aliasRepository = aliasRepository;
        this.userRepository = userRepository;
        this.spendingsRepository = spendingsRepository;
        this.commerciantsRepository = commerciantsRepository;
        this.splitsRepository = splitsRepository;
        this.output = output;
    }

    /**
     * Sets the service dependencies for the factory.
     *
     * @param userServiceParam        the user service
     * @param accountServiceParam     the account service
     * @param cardServiceParam        the card service
     * @param aliasServiceParam       the alias service
     * @param transactionServiceParam the transaction service
     */
    public void setServices(final UserService userServiceParam,
                            final AccountService accountServiceParam,
                            final CardService cardServiceParam,
                            final AliasService aliasServiceParam,
                            final TransactionService transactionServiceParam,
                            final CommerciantsService commerciantsServiceParam) {
        this.userService = userServiceParam;
        this.accountService = accountServiceParam;
        this.cardService = cardServiceParam;
        this.aliasService = aliasServiceParam;
        this.transactionService = transactionServiceParam;
        this.commerciantsService = commerciantsServiceParam;
        this.cashbackCalculator = new CashbackCalculator(commerciantsService,
                ExchangeService.getInstance(), accountRepository);
    }


    /**
     * Creates the appropriate Command object based on the input command.
     *
     * @param command The input command details.
     * @return A Command object ready for execution.
     */
    public Command createCommand(final CommandInput command) {
        if (command == null || command.getCommand() == null) {
            throw new IllegalArgumentException("Command input or command type cannot be null.");
        }

        switch (command.getCommand()) {
            case "addAccount":
                return new AddAccount(accountRepository, transactionRepository, userRepository,
                        cardRepository, aliasRepository, command);

            case "addFunds":
                return new AddFunds(accountRepository, command, transactionRepository);

            case "addInterest":
                return new AddInterest(accountRepository, transactionRepository, command, output);

            case "changeInterestRate":
                return new ChangeInterestRate(accountRepository, transactionRepository, command,
                        output);

            case "checkCardStatus":
                return new CheckCardStatus(accountRepository, cardRepository,
                        transactionRepository, command, output);

            case "createCard":
                return new CreateCard(accountRepository, cardRepository, transactionRepository,
                        command);

            case "createOneTimeCard":
                return new CreateOneTimeCard(accountRepository, cardRepository,
                        transactionRepository, command);

            case "deleteAccount":
                return new DeleteAccount(accountRepository, transactionRepository, userRepository,
                        command, output);

            case "deleteCard":
                return new DeleteCard(accountRepository, cardRepository, transactionRepository,
                        command, output);

            case "payOnline":
                return new PayOnline(accountRepository, cardRepository, transactionRepository,
                        spendingsRepository, commerciantsService, userRepository, command, output);

            case "printTransactions":
                return new PrintTransactions(transactionRepository, command, output,
                        userRepository);

            case "printUsers":
                return new PrintUsers(userService, accountService, cardService,
                        command, output);

            case "report":
                return new Report(accountRepository, transactionRepository, userRepository,
                        command, output);

            case "sendMoney":
                return new SendMoney(accountRepository, transactionRepository, aliasRepository,
                        command, output);

            case "setAlias":
                return new SetAlias(accountRepository, aliasRepository, command);

            case "setMinimumBalance":
                return new SetMinimumBalance(accountRepository, command);

            case "spendingsReport":
                return new SpendingsReport(accountRepository, cardRepository, userRepository,
                        transactionRepository, spendingsRepository, command, output);

            case "splitPayment":
                return new SplitPayment(accountRepository, transactionRepository,
                        splitsRepository, command);

            case "withdrawSavings":
                return new WithdrawSavings(accountRepository, transactionRepository,
                        command, output);

            case "upgradePlan":
                return new UpgradePlan(transactionRepository, accountRepository, command,
                        userRepository, output, ExchangeService.getInstance());

            case "cashWithdrawal":
                return new CashWithdrawal(accountRepository, cardRepository,
                        transactionRepository, command, output);

            case "acceptSplitPayment":
                return new AcceptSplitPayment(accountRepository, transactionRepository,
                        splitsRepository, command, output);

            case "addNewBusinessAssociate":
                return new AddNewBusinessAssociate(accountRepository, userRepository, command);

            case "changeSpendingLimit":
                return new ChangeSpendingLimit(accountRepository, command, output);

            case "businessReport":
                return new BusinessReport(accountRepository, transactionRepository,
                        userRepository, spendingsRepository, command, output);

            case "changeDepositLimit":
                return new ChangeDepositLimit(accountRepository, command, output);

            case "rejectSplitPayment":
                return new RejectSplitPayment(accountRepository, splitsRepository,
                        transactionRepository, command, output);


            default:
                throw new IllegalArgumentException("Unknown command: " + command.getCommand());
        }
    }

}
