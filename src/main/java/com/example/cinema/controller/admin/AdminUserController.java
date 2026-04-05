package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.request.CreateStaffRequest;
import com.example.cinema.model.dto.request.UpdateUserRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.UserResponse;
import com.example.cinema.model.enums.Role;
import com.example.cinema.service.user.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<UserResponse>> createStaffAccount(@RequestBody CreateStaffRequest request) {
        Role role = request.getRole() != null ? Role.valueOf(request.getRole()) : Role.STAFF;
        UserResponse created = userService.createStaffAccount(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                role
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    /**
     * Toggle khóa/mở tài khoản.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.toggleUserStatus(id)));
    }

    /**
     * Cập nhật tài khoản.
     * Body: { "email": "...", "role": "STAFF" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(id, request.getEmail(), request.getRole())));
    }

    /**
     * Gửi yêu cầu đổi mật khẩu qua email cho nhân viên.
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> requestResetPassword(@PathVariable Long id) {
        userService.sendResetPasswordEmail(id);
        return ResponseEntity.ok(ApiResponse.ok("Link đổi mật khẩu đã được gửi đến Gmail của nhân viên."));
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
