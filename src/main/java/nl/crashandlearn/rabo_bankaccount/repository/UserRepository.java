package nl.crashandlearn.rabo_bankaccount.repository;

import nl.crashandlearn.rabo_bankaccount.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    @Query(value = "SELECT SUM(a.balance) FROM Account a WHERE a.user.id = ?1")
    Double getBalance(Long user_id);
}