package com.example.cinema.exception;

/**
 * Exception ném khi không tìm thấy tài nguyên trong database.
 * Kế thừa AppException với HTTP code 404.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " không tìm thấy với ID: " + id, 404);
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " không tìm thấy với " + field + ": " + value, 404);
    }
}
