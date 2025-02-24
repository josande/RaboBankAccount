package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import nl.crashandlearn.rabo_bankaccount.exception.InsufficientFundsException;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
class AccountServiceTest {
    @Mock
    private AccountRepository repository;
    @Mock
    private AuthenticationHelperService authHelper;



    @Test
    void delete_doNothingIfAlreadyDeleted() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        AccountService service = new AccountService(repository, authHelper);

        service.delete(1L);

        verify(repository, never()).deleteById(1L);

    }

    @Test
    void delete_canDeleteAsOwner() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        when(authHelper.isOwner(account)).thenReturn(true);

        service.delete(1L);

        verify(repository, atLeastOnce()).deleteById(1L);
    }

    @Test
    void accountWithdrawal_shouldThrowExceptionIfAccountNotFound() {
        AccountService service = new AccountService(repository, authHelper);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("Could not find Account with id: 1", exception.getMessage());
    }
    @Test
    void accountWithdrawal_shouldThrowExceptionIfInsufficientFunds() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().balance(10.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("Insufficient balance on account: 1 Balance: 10.0 amount: 100.0", exception.getMessage());
    }

    @Test
    void accountWithdrawal_shouldThrowExceptionIfNotOwnerOrAdmin() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.isOwner(any(Account.class))).thenReturn(false);

        Exception exception = assertThrows(AccessDeniedException.class, () -> service.accountWithdrawal(100.0d, 1L));
        assertEquals("User lacks permission to withdraw from account: 1", exception.getMessage());
    }

    @Test
    void accountWithdrawal_shouldUpdateRepository_ifOwner() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isOwner(any(Account.class))).thenReturn(true);

        service.accountWithdrawal(100.0d, 1L);

        verify(repository).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void accountWithdrawal_shouldUpdateRepository_ifAdmin() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(authHelper.isAdmin()).thenReturn(true);

        service.accountWithdrawal(100.0d, 1L);

        verify(repository).save(ArgumentMatchers.any(Account.class));
    }




    @Test
    void accountTransfer_shouldThrowExceptionIfAccountFromNotFound() {
        AccountService service = new AccountService(repository, authHelper);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Account with id: 1", exception.getMessage());
    }
    @Test
    void accountTransfer_shouldThrowExceptionIfAccountToNotFound() {
        AccountService service = new AccountService(repository, authHelper);
        Account account = Account.builder().id(1L).balance(200.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Could not find Account with id: 2", exception.getMessage());
    }
    @Test
    void accountTransfer_shouldThrowExceptionIfInsufficientFunds() {
        AccountService service = new AccountService(repository, authHelper);
        Account accountFrom = Account.builder().id(1L).balance(10.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> service.accountTransfer(100.0d, 1L, 2L));
        assertEquals("Insufficient balance on account: 1 Balance: 10.0 amount: 100.0", exception.getMessage());
    }

    @Test
    void accountTransfer_shouldThrowExceptionIfNotOwnerOrAdmin() {
        AccountService service = new AccountService(repository, authHelper);
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
        AccountService service = new AccountService(repository, authHelper);
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
        AccountService service = new AccountService(repository, authHelper);
        Account accountFrom = Account.builder().id(1L).balance(200.0d).build();
        Account accountTo = Account.builder().id(2L).balance(100.0d).build();
        when(repository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(repository.findById(2L)).thenReturn(Optional.of(accountTo));

        when(authHelper.isAdmin()).thenReturn(true);

        service.accountTransfer(100.0d, 1L, 2L);

        verify(repository, times(2)).save(ArgumentMatchers.any(Account.class));
    }
}