package com.example.cinema.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
<<<<<<< HEAD

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    public LoginRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

=======
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
>>>>>>> feature/Customer
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
