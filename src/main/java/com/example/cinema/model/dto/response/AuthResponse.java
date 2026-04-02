package com.example.cinema.model.dto.response;

<<<<<<< HEAD
/**
 * DTO trả về khi đăng nhập thành công, chứa JWT token và thông tin role.
 */
public class AuthResponse {

    private String token;
    private String role;
    private String username;

    public AuthResponse() {}

    public AuthResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
=======
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String fullName;

    public AuthResponse() {}

    public AuthResponse(String token, String username, String role, String fullName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
>>>>>>> feature/Customer
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
<<<<<<< HEAD

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
=======
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
>>>>>>> feature/Customer
}
