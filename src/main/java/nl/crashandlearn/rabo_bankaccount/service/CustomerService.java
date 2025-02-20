package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.CustomerNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.Customer;
import nl.crashandlearn.rabo_bankaccount.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final AccountService accountService;

    public CustomerService(CustomerRepository repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
    }

    public Optional<Customer> findById(long id) {
        return repository.findById(id);
    }

    public Customer createCustomer(Customer customer) {
        return repository.save(customer);

    }

    public Optional<Customer> update(Customer newValue) {
        if(repository.existsById(newValue.getId()))
            return Optional.of(repository.save(newValue));
        return Optional.empty();
    }
    @Transactional
    public Optional<Customer> addAccount(Long customerId) throws CustomerNotFoundException {
        Optional<Customer> _customer = repository.findById(customerId);
        if(_customer.isPresent()) {
            Customer customer = _customer.get();
            Account account = Account.builder().customer(customer).build();
            accountService.createAccount(account);
            //customer.getAccounts().add(account);
            //return update(customer);
            return findById(customerId);
        }
        return Optional.empty();
    }


    }
