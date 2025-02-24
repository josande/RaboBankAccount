package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.*;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.Card;
import nl.crashandlearn.rabo_bankaccount.model.CardType;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import nl.crashandlearn.rabo_bankaccount.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AuthenticationHelperService authHelper;

    @Test
    void createCard_shouldThrowIfAccountNotFound() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.createCard(1L, CardType.CREDIT_CARD));
        assertEquals("Could not find Account with id: 1", exception.getMessage());
    }

    @Test
    void createCard_shouldThrowExceptionIfNotOwnerOrAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.createCard(1L, CardType.CREDIT_CARD));
        assertEquals("User lacks permission to create card for account: 1", exception.getMessage());
    }

    @Test
    void createCard_shouldThrowExceptionIfCardAlreadyPresent() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Card card = Card.builder().build();
        Account account = Account.builder().id(1L).balance(200.0d).cards(Set.of(card)).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isAdmin()).thenReturn(true);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        Exception exception = assertThrows(CardAlreadyPresentException.class, () -> service.createCard(1L, CardType.CREDIT_CARD));
        assertEquals("There is already a card linked to account with id: 1", exception.getMessage());
    }

    @Test
    void createCard_canCreateIfOwner() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.createCard(1L, CardType.CREDIT_CARD);

        verify(cardRepository).save(ArgumentMatchers.any(Card.class));
    }

    @Test
    void createCard_canCreateIfAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isAdmin()).thenReturn(true);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        service.createCard(1L, CardType.CREDIT_CARD);

        verify(cardRepository).save(ArgumentMatchers.any(Card.class));
    }



    @Test
    void cardWithdrawal_shouldThrowExceptionIfAccountNotFound() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);

        Exception exception = assertThrows(CardNotFoundException.class, () -> service.cardWithdrawal(100.0d, 1L));
        assertEquals("Could not find Card with id: 1", exception.getMessage());
    }

    @Test
    void cardWithdrawal_shouldThrowExceptionIfInsufficientFunds() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.cardWithdrawal(200.0d, 1L));
        // amount is 202 since it's a credit card
        assertEquals("Insufficient balance on account: 1 Balance: 200.0 amount: 202.0", exception.getMessage());
    }
    @Test
    void cardWithdrawal_shouldThrowExceptionIfNotOwnerOrAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);
        when(authHelper.isAdmin()).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.cardWithdrawal(100.0d, 1L));
        // amount is 202 since it's a credit card
        assertEquals("User lacks permission to withdraw from card: 1", exception.getMessage());
    }

    @Test
    void  cardWithdrawal_canWithdrawAsOwner() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.cardWithdrawal(100.0d, 1L);

        verify(accountRepository).save(any(Account.class));
    }
    @Test
    void  cardWithdrawal_canWithdrawAsAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);
        when(authHelper.isAdmin()).thenReturn(true);

        service.cardWithdrawal(100.0d, 1L);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void cardTransfer_shouldThrowExceptionIfCardNotFound() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CardNotFoundException.class, () -> service.cardTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Card with id: 1", exception.getMessage());

    }

    @Test
    void cardTransfer_shouldThrowExceptionIfAccountNotFound() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.cardTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Account with id: 2", exception.getMessage());
    }

    @Test
    void cardTransfer_shouldThrowExceptionIfCardAccountIsSameAsToAccount() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Exception exception = assertThrows(SameAccountException.class, () -> service.cardTransfer(100.0d, 1L, 1L));
        assertEquals("Can not transfer to the same account, id: 1", exception.getMessage());
    }

    @Test
    void cardTransfer_shouldThrowExceptionIfInsufficientFunds() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).build()));
        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.cardTransfer(200.0d, 1L, 2L));
        // amount is 202 since it's a credit card
        assertEquals("Insufficient balance on account: 1 Balance: 200.0 amount: 202.0", exception.getMessage());
    }

    @Test
    void cardTransfer_shouldThrowExceptionIfNotOwnerOrAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).build()));
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);
        when(authHelper.isAdmin()).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.cardTransfer(100.0d, 1L, 2L));
        assertEquals("User lacks permission to transfer from account: 1", exception.getMessage());
    }

    @Test
    void cardTransfer_canTransferAsOwner() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).build()));
        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.cardTransfer(100.0d, 1L, 2L);
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void cardTransfer_canTransferAsAdmin() {
        CardService service = new CardService(cardRepository, accountRepository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        Card card = Card.builder().cartType(CardType.CREDIT_CARD).account(account).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).build()));
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);
        when(authHelper.isAdmin()).thenReturn(true);

        service.cardTransfer(100.0d, 1L, 2L);
        verify(accountRepository, times(2)).save(any(Account.class));
    }




}