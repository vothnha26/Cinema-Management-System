package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.UserResponse;
import com.example.cinema.model.enums.Role;
import com.example.cinema.service.user.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý User dành riêng cho ADMIN (SRP).
 * Chỉ xử lý HTTP, delegate logic sang IUserService (DIP).
 * Route được bảo vệ bởi SecurityConfig: /api/admin/** -> ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final IUserService userService;

    public AdminUserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) Role role) {
        List<UserResponse> users = (role != null)
                ? userService.getUsersByRole(role)
                : userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    /**
     * Admin tạo tài khoản Staff/Manager.
     * Body: { "username": "...", "email": "...", "password": "...", "role": "STAFF" }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createStaffAccount(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        Role role = Role.valueOf(body.getOrDefault("role", "STAFF"));

        UserResponse created = userService.createStaffAccount(username, email, password, role);
        return ResponseEntity.ok(ApiResponse.ok(created));
    }

    /**
     * Toggle khóa/mở tài khoản.
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.toggleUserStatus(id)));
    }

    /**
     * Cập nhật tài khoản.
     * Body: { "email": "...", "role": "STAFF" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String email = body.get("email");
        Role role = Role.valueOf(body.get("role"));
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(id, email, role)));
    }

    /**
     * Xóa tài khoản.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
