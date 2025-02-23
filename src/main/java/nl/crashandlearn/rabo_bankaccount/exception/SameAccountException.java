package nl.crashandlearn.rabo_bankaccount.exception;

public class SameAccountException extends RuntimeException {
    public SameAccountException(Long id) {
        super("Can not transfer to the same account, id: " + id);
    }
}