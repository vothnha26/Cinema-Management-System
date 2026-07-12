package com.example.cinema.service.auth;

import com.example.cinema.model.dto.request.ChangePasswordRequest;
import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.request.VerifyOtpRequest;
import com.example.cinema.model.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse login(LoginRequest request);

    String register(RegisterRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    void requestPasswordReset(String email);

    void resetPasswordWithToken(String token, String newPassword);
}
