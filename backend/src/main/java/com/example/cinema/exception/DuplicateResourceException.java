package com.example.cinema.exception;

/**
 * Exception ném khi dữ liệu bị trùng lặp (ví dụ: username đã tồn tại).
 * Kế thừa AppException với HTTP code 409 (Conflict).
 */
public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String field, String value) {
        super(field + " '" + value + "' đã tồn tại trong hệ thống.", 409);
    }
}
