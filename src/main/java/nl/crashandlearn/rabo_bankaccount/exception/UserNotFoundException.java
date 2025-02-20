package nl.crashandlearn.rabo_bankaccount.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Could not find User with id: " + id);
    }
    public UserNotFoundException(String username) {
        super("Could not find User with username: " + username);
    }
}
