package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.annotation.Audited;
import nl.crashandlearn.rabo_bankaccount.exception.*;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.Card;
import nl.crashandlearn.rabo_bankaccount.model.CardType;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import nl.crashandlearn.rabo_bankaccount.repository.CardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CardService  extends BaseService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardService(CardRepository cardRepository, AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    public void createCard(Long accountId, CardType cardType) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        if (!isOwner(account) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to create card for account: "+accountId);

        if(account.getCards() != null && !account.getCards().isEmpty() )
            throw new CardAlreadyPresentException(accountId);

        Card card = Card.builder().account(account).cartType(cardType).build();
        cardRepository.save(card);
    }

    public void removeCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));

        if (!isOwner(card.getAccount()) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to remove card with id: "+cardId);

        cardRepository.delete(card);
    }

    @Audited
    public void cardWithdrawal(double amountToWithdraw, Long cardIdFrom) {
        var card = cardRepository.findById(cardIdFrom).orElseThrow(() -> new CardNotFoundException(cardIdFrom));
        var account = card.getAccount();
        if(account.getBalance() < amountToWithdraw * card.getCartType().getFee())
            throw new InsufficientFundsException(account.getId(), account.getBalance(), amountToWithdraw);

        if (!isOwner(card.getAccount()) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to withdraw from card: "+cardIdFrom);

        account.setBalance(account.getBalance() - amountToWithdraw * card.getCartType().getFee());
        accountRepository.save(account);
    }

    @Audited
    public void cardTransfer(double amount, Long cardIdFrom, Long accountIdTo) {
        var card = cardRepository.findById(cardIdFrom).orElseThrow(() -> new CardNotFoundException(cardIdFrom));
        var accountFrom = card.getAccount();
        var accountTo = accountRepository.findById(accountIdTo).orElseThrow(() -> new AccountNotFoundException(accountIdTo));

        if(accountFrom.equals(accountTo))
            throw new SameAccountException(accountFrom.getId());

        if(accountFrom.getBalance() < amount * card.getCartType().getFee())
            throw new InsufficientFundsException(accountFrom.getId(), accountFrom.getBalance(), amount);

        if (!isOwner(accountFrom) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to transfer from account: "+accountFrom);

        accountFrom.setBalance(accountFrom.getBalance() - amount * card.getCartType().getFee());
        accountTo.setBalance(accountTo.getBalance() + amount);
        accountRepository.save(accountTo);
        accountRepository.save(accountFrom);
    }
}
