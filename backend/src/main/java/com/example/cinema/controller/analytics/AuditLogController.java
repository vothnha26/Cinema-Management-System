package com.example.cinema.controller.analytics;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.AuditLogResponse;
import com.example.cinema.repository.analytics.AuditLogRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final ModelMapper modelMapper;

    public AuditLogController(AuditLogRepository auditLogRepository, ModelMapper modelMapper) {
        this.auditLogRepository = auditLogRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllLogs() {
        List<AuditLogResponse> logs = auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(log -> modelMapper.map(log, AuditLogResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
