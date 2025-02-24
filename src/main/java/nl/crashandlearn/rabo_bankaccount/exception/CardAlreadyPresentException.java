package nl.crashandlearn.rabo_bankaccount.exception;

public class CardAlreadyPresentException extends RuntimeException {
    public CardAlreadyPresentException(Long id) {
        super("There is already a card linked to account with id" + id);
    }
}
