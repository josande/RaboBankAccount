package nl.crashandlearn.rabo_bankaccount.repository;

import nl.crashandlearn.rabo_bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = ?1")
    Optional<Account> findById(Long userId);

    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.user.id = ?1")
    List<Account> findByUser(Long userId);
}
