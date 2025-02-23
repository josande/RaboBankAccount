package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.UserNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.AuditPost;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.AuditRepository;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService extends BaseService{

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;

    public AuditService(AuditRepository auditRepository, UserRepository userRepository) {
        super();
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    public void logRecord(String operation, String parameters, String result) {
        User createdBy = User.builder().id(getUserId()).build();

        AuditPost auditPost = AuditPost.builder()
                .createdBy(createdBy)
                .operation(operation)
                .parameters(parameters)
                .result(result)
                .build();
        auditRepository.save(auditPost);
    }

    public List<AuditPost> getAllAuditPosts() {
        return auditRepository.findAll();
    }
    public List<AuditPost> getAllAuditPostsForUser(long userId) {
        if(!userRepository.existsById(userId))
            throw new UserNotFoundException(userId);

        return auditRepository.findByUser(userId);
    }
}
