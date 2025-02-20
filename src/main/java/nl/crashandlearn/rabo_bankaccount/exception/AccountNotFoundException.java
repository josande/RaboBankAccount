package nl.crashandlearn.rabo_bankaccount.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Could not find Account with id: " + id);
    }
}
