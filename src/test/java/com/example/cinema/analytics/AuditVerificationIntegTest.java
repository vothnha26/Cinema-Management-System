package com.example.cinema.analytics;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.entity.AuditLog;
import com.example.cinema.repository.analytics.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AuditVerificationIntegTest extends BaseIntegTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private com.example.cinema.service.movie.MovieService movieService;

    @Test
    @DisplayName("Phase 5.1: Kiểm tra ghi nhận Audit Log sau khi tạo phim")
    void testAuditLogRecorded() {
        // 1. Thực hiện một hành động có @LogAction
        com.example.cinema.model.dto.request.MovieRequest request = new com.example.cinema.model.dto.request.MovieRequest();
        request.setTitle("Audit Test Movie"); request.setDuration(100);
        movieService.createMovie(request, null);

        // 2. Kiểm tra xem có log nào được ghi lại không
        List<AuditLog> logs = auditLogRepository.findAll();
        
        logs.forEach(log -> System.out.println("LOG: " + log.getAction() + " | Target: " + log.getTarget()));
        
        assertFalse(logs.isEmpty(), "Audit Log không được ghi lại sau khi gọi createMovie.");
    }
}
