package com.example.cinema.service;

import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.response.AuthResponse;

public interface AuthService {
    void registerUser(RegisterRequest request);
    AuthResponse authenticateUser(LoginRequest request);
}
