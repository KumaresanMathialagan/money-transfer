package com.account.moneytranser.aspect;

import com.account.moneytranser.entity.AuditLog;
import com.account.moneytranser.repositories.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper; // Used for JSON serialization

    @Before("execution(* com.account.moneytranser.service.TransferService.*(..))")
    public void logBefore(JoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String params = objectMapper.writeValueAsString(args);

        AuditLog auditLog = new AuditLog();
        auditLog.setMethod(methodName);
        auditLog.setParams(params);

        auditLogRepository.save(auditLog);
    }

    @AfterReturning(pointcut = "execution(* com.account.moneytranser.service.TransferService.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String resultStr = objectMapper.writeValueAsString(result);

        AuditLog auditLog = new AuditLog();
        auditLog.setMethod(methodName);
        auditLog.setResult(resultStr);
        auditLogRepository.save(auditLog);
    }
}
