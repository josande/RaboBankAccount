package nl.crashandlearn.rabo_bankaccount.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long id, double balance, double amount) {
        super("Insufficient balance on account: %s Balance: %s amount: %s".formatted(id, balance, amount));
    }
}
