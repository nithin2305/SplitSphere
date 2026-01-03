package com.splitsphere.service;

import com.splitsphere.model.AuditLog;
import com.splitsphere.model.User;
import com.splitsphere.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public void log(String action, String entityType, Long entityId, User user, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setUser(user);
        auditLog.setDetails(details);
        
        auditLogRepository.save(auditLog);
    }
}
