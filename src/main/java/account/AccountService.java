package account;

import config.TransactionHelper;
import org.springframework.stereotype.Component;
import user.User;
import java.util.Comparator;
import java.util.List;

@Component
public class AccountService {
    private final AccountProperties accountProperties;
    private final TransactionHelper transactionHelper;

    public AccountService(AccountProperties accountProperties, TransactionHelper transactionHelper) {
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    public Account createDefaultAccount(User user) {
        return accountCreate(user, accountProperties.getDefaultAmount());
    }

    public Account createAnotherAccount(User user) {
        return accountCreate(user, 0);
    }

    private Account accountCreate(User user, int initBalance) {
        return transactionHelper.executeTransaction(session -> {
            Account account = new Account();
            account.setUser(user);
            account.setMoneyAmount(initBalance);
            session.persist(account);
            return account;
        });
    }

    public CloseAccountResult accountClose(int accountId) {
        return transactionHelper.executeTransaction(session -> {
            Account accountToClose = getAccountById(accountId);
            User user =  accountToClose.getUser();
            List<Account> allUserAccounts = user.getAccounts();
            if (allUserAccounts.size() <= 1) {
                throw new IllegalStateException("Cannot close the only account for user "
                        + user.getLogin() + ", ID= " + user.getId());
            }

            Account targetAccount = allUserAccounts.stream()
                    .filter(account -> account.getId() != accountId)
                    .min(Comparator.comparingInt(Account::getId))
                    .orElseThrow(() -> new IllegalStateException("No target account found."));

            TransferResult transferResult = null;
            if (accountToClose.getMoneyAmount() > 0) {
                transferResult = accountTransfer(
                        accountToClose.getId(),
                        targetAccount.getId(),
                        accountToClose.getMoneyAmount()
                );
            }

            allUserAccounts.remove(accountToClose);
            session.remove(accountToClose);
            return new CloseAccountResult(accountId, user.getId(), targetAccount.getId(), transferResult);
        });
    }

    public Account accountDeposit(int accountId, int amount) {
        validateAmount(amount);
        return transactionHelper.executeTransaction(session -> {
            Account account = getAccountById(accountId);
            account.setMoneyAmount(Math.addExact(amount, account.getMoneyAmount()));
            return account;
        });
    }

    public Account accountWithdraw(int accountId, int amount) {
        validateAmount(amount);
        return transactionHelper.executeTransaction(session -> {
            Account account = getAccountById(accountId);

            if (account.getMoneyAmount() < amount) {
                throw new IllegalStateException("Insufficient funds on account ID= " + accountId
                        + ". Current balance: " + account.getMoneyAmount());
            }

            account.setMoneyAmount(account.getMoneyAmount() - amount);
            return account;
        });
    }

    public TransferResult accountTransfer(int fromId, int toId, int amount) {
        validateAmount(amount);
        if (fromId == toId) {
            throw new IllegalArgumentException("Cannot transfer money to the same account (ID= " + fromId + ").");
        }

        return transactionHelper.executeTransaction(session -> {
            Account fromAccount = getAccountById(fromId);
            Account toAccount = getAccountById(toId);

            if (fromAccount.getMoneyAmount() < amount) {
                throw new IllegalStateException("Insufficient funds for transfer on account ID= " + fromId
                        + ". Current balance: " + fromAccount.getMoneyAmount());
            }

            boolean isSameUser = fromAccount.getUser().getId() == toAccount.getUser().getId();
            double rate = isSameUser ? 0.0 : accountProperties.getTransferCommission();
            int amountToTransfer = (int) Math.round(amount * (1 - rate));
            int commissionInAmount = amount - amountToTransfer;

            fromAccount.setMoneyAmount(fromAccount.getMoneyAmount() - amount);
            toAccount.setMoneyAmount(Math.addExact(toAccount.getMoneyAmount(),amountToTransfer));

            return new TransferResult(fromAccount, toAccount, amount, amountToTransfer, isSameUser, commissionInAmount);
        });
    }

    public Account getAccountById(int accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
        return transactionHelper.executeTransaction(session -> {
            Account account = session.find(Account.class, accountId);
            if (account == null) {
                throw new IllegalArgumentException("Account with ID=" + accountId + " not found.");
            }
            return account;
        });
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }
}