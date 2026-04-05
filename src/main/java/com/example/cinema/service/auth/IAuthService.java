package com.example.cinema.service.auth;

import com.example.cinema.model.dto.request.ChangePasswordRequest;
import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.response.AuthResponse;

/**
 * Interface xác thực (DIP: Controller phụ thuộc vào abstraction, không phụ
 * thuộc vào implementation).
 */
public interface IAuthService {

    /**
     * Xác thực tài khoản và cấp JWT token.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Đăng ký tài khoản khách hàng (Customer).
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Đổi mật khẩu cho người dùng hiện tại.
     */
    void changePassword(String username, ChangePasswordRequest request);

    /**
     * Yêu cầu đặt lại mật khẩu: Sinh token, lưu Redis và gửi Email.
     */
    void requestPasswordReset(String email);

    /**
     * Xác thực token và đặt mật khẩu mới.
     */
    void resetPasswordWithToken(String token, String newPassword);
}
