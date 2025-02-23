package nl.crashandlearn.rabo_bankaccount.aspect;

import nl.crashandlearn.rabo_bankaccount.model.AuditPost;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.AuditRepository;
import nl.crashandlearn.rabo_bankaccount.security.userservice.UserDetail;
import nl.crashandlearn.rabo_bankaccount.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;



@Aspect
@Component
public class AuditedAspect {
    private final AuditService auditService;

    public AuditedAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning("@annotation(nl.crashandlearn.rabo_bankaccount.annotation.Audited)")
    public void createAuditTrailForSuccess(JoinPoint joinPoint) {
        logRecord(joinPoint, null);
    }

    @AfterThrowing(value = "@annotation(nl.crashandlearn.rabo_bankaccount.annotation.Audited)", throwing = "ex")
    public void createAuditTrailForFailure(JoinPoint joinPoint, Exception ex) {
        logRecord(joinPoint, ex);
    }

    private void logRecord(JoinPoint joinPoint, Exception exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StringBuilder arguments = new StringBuilder();
        for(int i=0; i<joinPoint.getArgs().length; i++) {
            if (arguments.length() != 0)
                arguments.append(", ");
            arguments.append(signature.getParameterNames()[i]).append("=").append(joinPoint.getArgs()[i].toString());
        }
        String result = exception==null ? "Ok" : exception.getMessage();

        auditService.logRecord(signature.getMethod().getName(), arguments.toString(), result);
    }

}
