package nl.crashandlearn.rabo_bankaccount.repository;

import nl.crashandlearn.rabo_bankaccount.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {}