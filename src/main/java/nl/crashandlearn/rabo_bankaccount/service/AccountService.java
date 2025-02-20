package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Optional<Account> findById(long id) {
        return repository.findById(id);
    }

    public Account createAccount(Account account) {
        return repository.save(account);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Account> getAllAccounts() {
        return repository.findAll();
    }

    public Optional<Account> update(Account newValue) {
        if(repository.existsById(newValue.getId()))
            return Optional.of(repository.save(newValue));
        return Optional.empty();
    }
}
