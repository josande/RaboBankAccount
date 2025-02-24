package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import nl.crashandlearn.rabo_bankaccount.exception.CardNotFoundException;
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

        Card card = Card.builder().account(account).cartType(cardType).build();
        cardRepository.save(card);
    }

    public void removeCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));

        if (!isOwner(card.getAccount()) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to remove card with id: "+cardId);

        cardRepository.delete(card);
    }
}
