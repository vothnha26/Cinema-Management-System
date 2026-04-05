package com.example.cinema.controller.auth;

import com.example.cinema.model.dto.request.ChangePasswordRequest;
import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.request.VerifyOtpRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.service.auth.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller xác thực (SRP: Chỉ nhận HTTP request và trả response).
 * Logic nghiệp vụ được delegate hoàn toàn sang IAuthService (DIP).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        authService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
