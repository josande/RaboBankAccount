package nl.crashandlearn.rabo_bankaccount.repository;

import nl.crashandlearn.rabo_bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT * FROM ACCOUNT WHERE user_id = ?1", nativeQuery = true)
    List<Account> findByUser(Long userId);
}
