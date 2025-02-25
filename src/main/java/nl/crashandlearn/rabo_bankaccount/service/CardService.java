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
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final AuthenticationHelperService authHelper;

    public CardService(CardRepository cardRepository, AccountRepository accountRepository, AuthenticationHelperService authHelper) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.authHelper = authHelper;
    }

    public Card createCard(Long accountId, CardType cardType) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        if (!authHelper.isOwner(account) && !authHelper.isAdmin())
            throw new AccessDeniedException("User lacks permission to create card for account: "+accountId);

        if(account.getCards() != null && !account.getCards().isEmpty() )
            throw new CardAlreadyPresentException(accountId);

        Card card = Card.builder().account(account).cartType(cardType).build();
        return cardRepository.save(card);
    }

    public void removeCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));

        if (!authHelper.isOwner(card.getAccount()) && !authHelper.isAdmin())
            throw new AccessDeniedException("User lacks permission to remove card with id: "+cardId);

        cardRepository.delete(card);
    }

    @Audited
    public void cardWithdrawal(double amountToWithdraw, Long cardIdFrom) {
        var card = cardRepository.findById(cardIdFrom).orElseThrow(() -> new CardNotFoundException(cardIdFrom));
        var account = card.getAccount();
        if(account.getBalance() < amountToWithdraw * card.getCartType().getFee())
            throw new InsufficientFundsException(account.getId(), account.getBalance(), amountToWithdraw * card.getCartType().getFee());

        if (!authHelper.isOwner(card.getAccount()) && !authHelper.isAdmin())
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
            throw new InsufficientFundsException(accountFrom.getId(), accountFrom.getBalance(), amount * card.getCartType().getFee());

        if (!authHelper.isOwner(accountFrom) && !authHelper.isAdmin())
            throw new AccessDeniedException("User lacks permission to transfer from account: "+accountFrom.getId());

        accountFrom.setBalance(accountFrom.getBalance() - amount * card.getCartType().getFee());
        accountTo.setBalance(accountTo.getBalance() + amount);
        accountRepository.save(accountTo);
        accountRepository.save(accountFrom);
    }
}
