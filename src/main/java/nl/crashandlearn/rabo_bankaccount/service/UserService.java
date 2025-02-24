package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.UserNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final AuthenticationHelperService authHelper;

    public UserService(UserRepository repository, AuthenticationHelperService authHelper) {
        this.repository = repository;
        this.authHelper = authHelper;
    }

    public User getCurrentUser() {
        return repository.findById(authHelper.getUserId()).orElseThrow(() -> new UserNotFoundException(authHelper.getUserId()));
    }

    public double getBalance() {
        Double balance = repository.getBalance(authHelper.getUserId());
        return balance == null ? 0 : balance;
    }

    public Optional<User> findByIdWithAccounts(Long id) {
        return repository.findById(id);
    }

    public List<User> getAllUsersWithAccounts() {
        return repository.findAll();

    }
}
