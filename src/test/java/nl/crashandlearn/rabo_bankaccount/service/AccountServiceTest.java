package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import nl.crashandlearn.rabo_bankaccount.exception.InsufficientFundsException;
import nl.crashandlearn.rabo_bankaccount.exception.SameAccountException;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.Card;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import nl.crashandlearn.rabo_bankaccount.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class AccountServiceTest {
    @Mock
    private AccountRepository repository;
    @Mock
    private AuthenticationHelperService authHelper;
    @Mock
    private CardRepository cardRepository;

    @Test
    void delete_doNothingIfAlreadyDeleted() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        AccountService service = new AccountService(repository, authHelper, cardRepository);

        service.delete(1L);

        verify(repository, never()).deleteById(1L);
    }

    @Test
    void delete_canDeleteAsOwner() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isOwner(account)).thenReturn(true);

        service.delete(1L);

        verify(repository, atLeastOnce()).deleteById(1L);
    }

    @Captor
    private ArgumentCaptor<Iterable<Card>> cardCaptor;

    @Test
    void delete_alsoRemovedLinkedCards() {
        Card card1 = Card.builder().id(1L).build();
        Card card2 = Card.builder().id(2L).build();
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).cards(Set.of(card1, card2)).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isOwner(account)).thenReturn(true);

        service.delete(1L);

        verify(cardRepository).deleteAll(cardCaptor.capture());
        Iterable<Card> deletedCards = cardCaptor.getValue();
        assertTrue(StreamSupport.stream(deletedCards.spliterator(), false).anyMatch(card -> Objects.equals(card.getId(), card1.getId())));
        assertTrue(StreamSupport.stream(deletedCards.spliterator(), false).anyMatch(card -> Objects.equals(card.getId(), card2.getId())));
    }

    @Test
    void accountWithdrawal_shouldThrowExceptionIfAccountNotFound() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("Could not find Account with id: 1", exception.getMessage());
    }
    @Test
    void accountWithdrawal_shouldThrowExceptionIfInsufficientFunds() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().balance(10.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("Insufficient balance on account: 1 Balance: 10.0 amount: 100.0", exception.getMessage());
    }

    @Test
    void accountWithdrawal_shouldThrowExceptionIfNotOwnerOrAdmin() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("User lacks permission to withdraw from account: 1", exception.getMessage());
    }

    @Test
    void accountWithdrawal_shouldUpdateRepository_ifOwner() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.accountWithdrawal(100.0d, 1L);

        verify(repository).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void accountWithdrawal_shouldUpdateRepository_ifAdmin() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isAdmin()).thenReturn(true);

        service.accountWithdrawal(100.0d, 1L);

        verify(repository).save(ArgumentMatchers.any(Account.class));
    }




    @Test
    void accountTransfer_shouldThrowExceptionIfAccountFromNotFound() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Account with id: 1", exception.getMessage());
    }
    @Test
    void accountTransfer_shouldThrowExceptionIfAccountToNotFound() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Account with id: 2", exception.getMessage());
    }

    @Test
    void accountTransfer_shouldThrowExceptionIfFromAndToAccountAreTheSame() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account accountFrom = Account.builder().id(1L).balance(10.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));

        Exception exception = assertThrows(SameAccountException.class, () -> service.accountTransfer(100.0d, 1L, 1L));
        assertEquals("Can not transfer to the same account, id: 1", exception.getMessage());
    }


    @Test
    void accountTransfer_shouldThrowExceptionIfInsufficientFunds() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account accountFrom = Account.builder().id(1L).balance(10.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Insufficient balance on account: 1 Balance: 10.0 amount: 100.0", exception.getMessage());
    }

    @Test
    void accountTransfer_shouldThrowExceptionIfNotOwnerOrAdmin() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account accountFrom = Account.builder().id(1L).balance(200.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("User lacks permission to transfer from account: 1", exception.getMessage());
    }

    @Test
    void accountTransfer_shouldUpdateRepository_ifOwner() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account accountFrom = Account.builder().id(1L).balance(200.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));

        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.accountTransfer(100.0d, 1L, 2L);

        verify(repository, times(2)).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void accountTransfer_shouldUpdateRepository_ifAdmin() {
        AccountService service = new AccountService(repository, authHelper, cardRepository);
        Account accountFrom = Account.builder().id(1L).balance(200.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));

        when(authHelper.isAdmin()).thenReturn(true);

        service.accountTransfer(100.0d, 1L, 2L);

        verify(repository, times(2)).save(ArgumentMatchers.any(Account.class));
    }
}