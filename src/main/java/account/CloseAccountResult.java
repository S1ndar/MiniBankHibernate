package account;

public record CloseAccountResult(
        int closedAccountId,
        int userId,
        int targetAccountId,
        TransferResult transferResult   // null, если переводить было нечего
) {}