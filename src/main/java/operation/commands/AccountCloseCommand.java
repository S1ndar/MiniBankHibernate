package operation.commands;

import account.AccountService;
import account.TransferResult;
import console.ConsoleInput;
import operation.Commands;
import operation.OperationCommand;
import org.springframework.stereotype.Component;
import account.CloseAccountResult;

@Component
public class AccountCloseCommand implements OperationCommand {
    private final ConsoleInput consoleInput;
    private final AccountService accountService;
    public AccountCloseCommand(ConsoleInput consoleInput, AccountService accountService) {
        this.consoleInput = consoleInput;
        this.accountService = accountService;
    }

    @Override
    public void execute() {
        int accountId = consoleInput.readInt("Account ID: ");
        CloseAccountResult result = accountService.accountClose(accountId);

        StringBuilder sb = new StringBuilder();
        sb.append("Account ").append(result.closedAccountId()).append(" closed.");

        if (result.transferResult() != null) {
            TransferResult t = result.transferResult();
            sb.append(" Balance transferred to account ID= ").append(result.targetAccountId())
                    .append(": ").append(t.amountToTransfer()).append(" (commission ").append(t.commission()).append(")");
        } else {
            sb.append(" Balance was zero, nothing to transfer.");
        }

        System.out.println(sb);
    }

    @Override
    public Commands getCommand() {
        return Commands.ACCOUNT_CLOSE;
    }
}
