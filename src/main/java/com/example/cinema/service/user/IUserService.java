package com.example.cinema.service.user;

import com.example.cinema.model.dto.response.UserResponse;
import com.example.cinema.model.enums.Role;

import java.util.List;

/**
 * Interface quản lý User (DIP): Admin dùng để CRUD tài khoản.
 */
public interface IUserService {

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    UserResponse getUserById(Long id);

    /**
     * Admin tạo tài khoản Staff/Manager.
     */
    UserResponse createStaffAccount(String username, String email, String password, Role role);

    /**
     * Khóa/Mở khóa tài khoản.
     */
    UserResponse toggleUserStatus(Long id);

    /**
     * Cập nhật thông tin staff/manager.
     */
    UserResponse updateUser(Long id, String email, Role role);

    /**
     * Xóa tài khoản (không cấp quyền xóa Admin).
     */
    void deleteUser(Long id);

    /**
     * Gửi Email Reset Mật khẩu cho nhân viên (SOLID: Sử dụng Notification Channel).
     */
    void sendResetPasswordEmail(Long userId);
}
