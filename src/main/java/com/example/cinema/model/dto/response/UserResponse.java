package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.Role;
import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin User cho Admin quản lý (không bao gồm password).
 */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean status;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
