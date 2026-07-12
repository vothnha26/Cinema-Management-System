package com.example.cinema.exception;

/**
 * Exception ném khi người dùng chưa xác thực hoặc token không hợp lệ.
 * Kế thừa AppException với HTTP code 401.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message, 401);
    }

    public UnauthorizedException() {
        super("Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.", 401);
    }
}
