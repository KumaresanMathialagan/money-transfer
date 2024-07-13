package com.account.moneytranser.repositories;

import com.account.moneytranser.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {

}
