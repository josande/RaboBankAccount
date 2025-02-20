package nl.crashandlearn.rabo_bankaccount.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username %s already exists.".formatted(username));
    }
}
