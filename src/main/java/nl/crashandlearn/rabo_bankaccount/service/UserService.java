package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.UserNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getCurrentUser() {
        return repository.findById(getUserId()).orElseThrow(() -> new UserNotFoundException(getUserId()));
    }

    public double getBalance() {
        Double balance = repository.getBalance(getUserId());;
        return balance == null ? 0 : balance;

    }
}
