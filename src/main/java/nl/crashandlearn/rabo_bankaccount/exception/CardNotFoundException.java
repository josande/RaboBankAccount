package nl.crashandlearn.rabo_bankaccount.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Could not find Card with id: " + id);
    }
}
