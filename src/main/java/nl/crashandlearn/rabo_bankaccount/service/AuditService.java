package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.exception.UserNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.AuditPost;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.AuditRepository;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final AuthenticationHelperService authHelper;

    public AuditService(AuditRepository auditRepository, UserRepository userRepository, AuthenticationHelperService authHelper) {
        super();
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.authHelper = authHelper;
    }

    public void logRecord(String operation, String parameters, String result) {
        User createdBy = User.builder().id(authHelper.getUserId()).build();

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
