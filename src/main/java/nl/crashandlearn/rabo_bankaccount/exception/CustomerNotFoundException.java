package nl.crashandlearn.rabo_bankaccount.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Could not find Customer with id: " + id);
    }
}
