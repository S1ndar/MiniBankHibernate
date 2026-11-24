package user;

import account.Account;
import account.AccountService;
import config.TransactionHelper;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UserService {
    private final AccountService accountService;
    private final TransactionHelper transactionHelper;

    public UserService(AccountService accountService, TransactionHelper transactionHelper) {
        this.accountService = accountService;
        this.transactionHelper = transactionHelper;
    }

    public User userCreate(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login cannot be null or blank.");
        }
        return transactionHelper.executeTransaction(session -> {
            Long count = session.createQuery("select count(u) from User u where u.login = :login", Long.class)
                    .setParameter("login", login)
                    .uniqueResult();
            if (count > 0) {
                throw new IllegalArgumentException("User already exists");
            }

            User user = new User(login);
            session.persist(user);
            user.getAccounts().add(accountService.createDefaultAccount(user));
            return user;
        });
    }

    public Account createAccountForUser(int userId) {
        return transactionHelper.executeTransaction(session -> {
            User user = getUserById(userId);
            Account newAccount = accountService.createAnotherAccount(user);
            user.getAccounts().add(newAccount);
            return newAccount;
        });
    }

    public User getUserById(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return transactionHelper.executeTransaction(session -> {
            User user = session.find(User.class,userId);
            if (user == null) {
                throw new IllegalArgumentException("User with ID= " + userId + " not found.");
            }
            return user;
        });
    }

    public List<User> getAllUsers() {
        return transactionHelper.executeTransaction(session ->
                session.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.accounts",
                User.class).list());
    }
}