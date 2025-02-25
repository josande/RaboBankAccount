package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.annotation.Audited;
import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import nl.crashandlearn.rabo_bankaccount.exception.InsufficientFundsException;
import nl.crashandlearn.rabo_bankaccount.exception.SameAccountException;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import nl.crashandlearn.rabo_bankaccount.repository.CardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService{

    private final AccountRepository repository;
    private final AuthenticationHelperService authHelper;
    private final CardRepository cardRepository;

    public AccountService(AccountRepository repository, AuthenticationHelperService authHelper, CardRepository cardRepository) {
        this.repository = repository;
        this.authHelper = authHelper;
        this.cardRepository = cardRepository;
    }

    public Optional<Account> findById(long id) {
        return repository.findById(id);
    }

    public Account createAccount(Double balance) {

        var account = Account.builder()
                .user(User.builder().id(authHelper.getUserId()).build())
                .balance(balance)
                .build();

        return repository.save(account);
    }

    @Transactional
    public void delete(Long id) {
        var account = repository.findById(id).orElse(null);
        if(account == null) return;

        if (authHelper.isOwner(account) || authHelper.isAdmin()) {
            if(account.getCards() != null) {
                cardRepository.deleteAll(account.getCards());
            }
            repository.deleteById(id);
        }
        else throw new AccessDeniedException("User lacks permission to delete account: "+id);
    }


    public List<Account> getAllAccountsForUser() {
        return repository.findByUserId(authHelper.getUserId());
    }


    public List<Account> getAllAccounts() {
        return repository.findAll();
    }


    @Audited
    public void accountWithdrawal(double amountToWithdraw, Long accountIdFrom) {
        var accountFrom = repository.findById(accountIdFrom).orElseThrow(() -> new AccountNotFoundException(accountIdFrom));

        if(accountFrom.getBalance()<amountToWithdraw)
            throw new InsufficientFundsException(accountIdFrom, accountFrom.getBalance(), amountToWithdraw);

        if (!authHelper.isOwner(accountFrom) && !authHelper.isAdmin())
            throw new AccessDeniedException("User lacks permission to withdraw from account: "+accountFrom.getId());

        accountFrom.setBalance(accountFrom.getBalance() - amountToWithdraw);
        repository.save(accountFrom);
    }


    @Transactional
    @Audited
    public void accountTransfer(double amountToTransfer, Long accountIdFrom, Long accountIdTo) {
        var accountFrom = repository.findById(accountIdFrom).orElseThrow(() -> new AccountNotFoundException(accountIdFrom));
        var accountTo = repository.findById(accountIdTo).orElseThrow(() -> new AccountNotFoundException(accountIdTo));

        if(accountFrom.equals(accountTo))
            throw new SameAccountException(accountIdFrom);

        if(accountFrom.getBalance()<amountToTransfer)
            throw new InsufficientFundsException(accountIdFrom, accountFrom.getBalance(), amountToTransfer);

        if (!authHelper.isOwner(accountFrom) && !authHelper.isAdmin())
            throw new AccessDeniedException("User lacks permission to transfer from account: "+accountFrom.getId());

        accountFrom.setBalance(accountFrom.getBalance() - amountToTransfer);
        accountTo.setBalance(accountTo.getBalance() + amountToTransfer);
        repository.save(accountTo);
        repository.save(accountFrom);
    }


}
