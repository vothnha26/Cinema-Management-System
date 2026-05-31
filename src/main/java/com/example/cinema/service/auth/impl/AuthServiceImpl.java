package com.example.cinema.service.auth.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.*;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.Role;
import com.example.cinema.security.JwtUtil;
import com.example.cinema.service.auth.IAuthService;
import com.example.cinema.service.infrastructure.facade.UserDomainFacade;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserDomainFacade userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserDomainFacade userRepo, AuthenticationManager authenticationManager, 
                           JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepo.findUserByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(user, user.getRole().name());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        return response;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepo.findUserByUsername(request.getUsername()).isPresent()) throw new AppException("Username đã tồn tại");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.CUSTOMER);
        
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        userRepo.saveCustomer(customer);
        return "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.";
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        // Implementation for OTP verification
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        // Implementation for changing password
    }

    @Override
    public void requestPasswordReset(String email) {
        // Implementation for requesting password reset
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        // Implementation for resetting password with token
    }
}
