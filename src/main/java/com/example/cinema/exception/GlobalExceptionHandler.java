package com.example.cinema.exception;

import com.example.cinema.model.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException e) {
        return ResponseEntity.status(e.getCode())
                .body(ApiResponse.error(e.getMessage(), e.getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        System.err.println(">>> VALIDATION FAILED: " + errors);
        return ResponseEntity.status(400)
                .body(ApiResponse.error("Dữ liệu không hợp lệ: " + errors.toString(), 400));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.error("Bạn không có quyền truy cập chức năng này", 403));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e) {
        e.printStackTrace(); // In ra log server để debug
        return ResponseEntity.status(500)
                .body(ApiResponse.error("Đã có lỗi hệ thống xảy ra: " + e.getMessage(), 500));
    }
}
