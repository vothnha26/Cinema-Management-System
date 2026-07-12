package com.example.cinema.config;

import com.example.cinema.model.entity.AuditLog;
import com.example.cinema.repository.analytics.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logAudit(JoinPoint joinPoint, LogAction logAction, Object result) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "SYSTEM";

            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setAction(logAction.action());
            log.setTarget(logAction.target());
            log.setTimestamp(LocalDateTime.now());
            
            // Có thể trích xuất ID từ kết quả trả về nếu cần
            log.setDetails("Thực hiện " + logAction.action() + " trên " + logAction.target());

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Không để lỗi ghi log làm ảnh hưởng đến luồng nghiệp vụ chính
            log.error("Lỗi khi ghi Audit Log: ", e);
        }
    }
}
