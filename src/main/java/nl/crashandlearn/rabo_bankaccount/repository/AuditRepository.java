package nl.crashandlearn.rabo_bankaccount.repository;

import nl.crashandlearn.rabo_bankaccount.model.AuditPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditPost, Long> {
    @Query("SELECT a FROM AuditPost a JOIN FETCH a.createdBy WHERE a.createdBy.id = ?1")
    List<AuditPost> findByUser(Long id);
}
