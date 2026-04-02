package com.example.cinema.exception;

import com.example.cinema.model.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Xử lý lỗi toàn cục cho tất cả REST API (SRP: Chỉ chịu trách nhiệm format lỗi).
 * Mọi Service/Controller trong hệ thống chỉ cần throw Exception,
 * lớp này sẽ tự động bắt và trả về JSON chuẩn ApiResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* --- Lỗi nghiệp vụ chung (400) --- */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException e) {
        return ResponseEntity.status(e.getCode())
            .body(ApiResponse.error(e.getMessage(), e.getCode()));
    }

    /* --- Lỗi validation request body (400) --- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(400)
            .body(ApiResponse.error(message, 400));
    }

    /* --- Lỗi phân quyền: user đã login nhưng không đủ role (403) --- */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(403)
            .body(ApiResponse.error("Bạn không có quyền truy cập tài nguyên này.", 403));
    }

    /* --- Lỗi không xác định (500) --- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error("Đã có lỗi xảy ra: " + e.getMessage(), 500));
    }
}
