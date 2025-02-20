package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import nl.crashandlearn.rabo_bankaccount.exception.InsufficientFundsException;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends BaseService {
    private final AccountRepository repository;


    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }



    public Optional<Account> findById(long id) {
        return repository.findById(id);
    }

    public Account createAccount(Account account) {
        account.setUser(User.builder().id(getUserId()).build());
        return repository.save(account);
    }

    public void delete(Long id) {
        var account = repository.findById(id).orElse(null);
        if(account == null) return;

        if (isOwner(account) || isAdmin()) repository.deleteById(id);
        else throw new AccessDeniedException("User lacks permission to delete account: "+id);
    }


    public List<Account> getAllAccountsForUser() {
        return repository.findByUser(getUserId());
    }


    public List<Account> getAllAccounts() {
        return repository.findAll();
    }

    public Account update(Account newValue) {
        var account = repository.findById(newValue.getId()).orElseThrow(() -> new AccountNotFoundException(newValue.getId()));

        if (isOwner(account) || isAdmin()) return repository.save(newValue);
        throw new AccessDeniedException("User lacks permission to update account: "+newValue.getId());

    }


    public void withdraw(Long accountIdFrom, double amountToWithdraw) {
        var accountFrom = repository.findById(accountIdFrom).orElseThrow(() -> new AccountNotFoundException(accountIdFrom));

        if(accountFrom.getBalance()<amountToWithdraw)
            throw new InsufficientFundsException(accountIdFrom, accountFrom.getBalance(), amountToWithdraw);

        if (!isOwner(accountFrom) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to withdraw from account: "+accountFrom);

        accountFrom.setBalance(accountFrom.getBalance() - amountToWithdraw);
        repository.save(accountFrom);
    }


    @Transactional
    public void transfer(Long accountIdFrom, Long accountIdTo, double amountToTransfer) {
        var accountFrom = repository.findById(accountIdFrom).orElseThrow(() -> new AccountNotFoundException(accountIdFrom));
        var accountTo = repository.findById(accountIdTo).orElseThrow(() -> new AccountNotFoundException(accountIdTo));

        if(accountFrom.getBalance()<amountToTransfer)
            throw new InsufficientFundsException(accountIdFrom, accountFrom.getBalance(), amountToTransfer);

        if (!isOwner(accountFrom) && !isAdmin())
            throw new AccessDeniedException("User lacks permission to transfer from account: "+accountFrom);

        accountFrom.setBalance(accountFrom.getBalance() - amountToTransfer);
        accountTo.setBalance(accountTo.getBalance() + amountToTransfer);
        repository.save(accountTo);
        repository.save(accountFrom);
    }


}
