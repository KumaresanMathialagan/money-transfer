package com.account.moneytranser.aspect;

import com.account.moneytranser.entity.AuditLog;
import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.repositories.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
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
    private ObjectMapper objectMapper;

    @Before("execution(* com.account.moneytranser.service.TransferService.transfer(..)) && args(moneyTransfer)")
    public void logBefore(JoinPoint joinPoint, MoneyTransfer moneyTransfer) throws JsonProcessingException {
        String methodName = joinPoint.getSignature().getName();
        String params = objectMapper.writeValueAsString(moneyTransfer);

        AuditLog auditLog = new AuditLog();
        auditLog.setMethod(methodName);
        auditLog.setParams(params);

        auditLogRepository.save(auditLog);
    }

}
