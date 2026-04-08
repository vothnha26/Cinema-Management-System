package com.example.cinema.model.dto.response;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String fullName;
    private Long branchId;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String role, String fullName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }

    public AuthResponse(String token, String username, String role, String fullName, Long branchId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.branchId = branchId;
    }

    public AuthResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
}
