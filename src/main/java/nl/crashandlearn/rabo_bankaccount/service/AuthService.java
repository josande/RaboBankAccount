package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.UsernameAlreadyExistsException;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }


    public User registerUser(User user) {
        if(repository.existsByUsername(user.getUsername()))
            throw new UsernameAlreadyExistsException(user.getUsername());

        return repository.save(user);

    }

}
