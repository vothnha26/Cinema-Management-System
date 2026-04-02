package com.example.cinema.exception;

public class AppException extends RuntimeException {
    private final int code;

    public AppException(String message) {
        super(message);
        this.code = 400;
    }

    public AppException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
