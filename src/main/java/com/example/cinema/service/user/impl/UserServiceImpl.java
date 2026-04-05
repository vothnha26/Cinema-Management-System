package com.example.cinema.service.user.impl;

import com.example.cinema.exception.DuplicateResourceException;
import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.dto.response.UserResponse;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.notification.INotificationService;
import com.example.cinema.service.user.IUserService;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation quản lý User (SRP: Chỉ xử lý CRUD tài khoản).
 * Không chứa logic xác thực (Authentication) hay phân quyền (Authorization).
 */
@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final INotificationService notificationService;
    private final com.example.cinema.service.auth.IAuthService authService;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper,
            INotificationService notificationService,
            @org.springframework.context.annotation.Lazy com.example.cinema.service.auth.IAuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN) // Ẩn ADMIN
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createStaffAccount(String username, String email, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email", email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // Nếu không truyền pass (Onboarding), dùng UUID để bảo mật
        String initialPassword = (password == null || password.isEmpty()) ? java.util.UUID.randomUUID().toString() : password;
        user.setPassword(passwordEncoder.encode(initialPassword));
        
        user.setRole(role);
        user.setStatus(true);

        User savedUser = userRepository.save(user);

        // TỰ ĐỘNG GỬI MAIL THIẾT LẬP MẬT KHẨU CHO NHÂN VIÊN MỚI
        try {
            authService.requestPasswordReset(email);
        } catch (Exception e) {
            System.err.println("⚠️ Không thể gửi mail Onboarding: " + e.getMessage());
        }

        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể khóa tài khoản ADMIN.");
        }
        user.setStatus(!user.getStatus());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, String email, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể sửa tài khoản ADMIN.");
        }

        if (role == Role.ADMIN) {
            throw new RuntimeException("Không thể nâng cấp lên ADMIN.");
        }

        user.setEmail(email);
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN.");
        }
        userRepository.delete(user);
    }

    @Override
    public void sendResetPasswordEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        // Tạo link đơn giản (trong thực tế cần mã hóa token và lưu vào DB/Redis)
        String resetLink = "http://localhost:8082/reset-password.html?email=" + user.getEmail();
        String subject = "🔑 Yêu cầu đặt lại mật khẩu - StarCinema";
        String body = "<p>Chào bạn,</p>"
                + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản StarCinema của bạn.</p>"
                + "<p>Vui lòng nhấn vào nút bên dưới để thực hiện thay đổi mật khẩu:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "  <a href='" + resetLink + "' style='background-color: #E5133A; color: white; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 5px;'>ĐẶT LẠI MẬT KHẨU</a>"
                + "</div>"
                + "<p>Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.</p>";

        // Sử dụng Strategy EMAIL qua NotificationService (DIP)
        notificationService.sendNotification(user.getEmail(), subject, body, "EMAIL");
    }

    private UserResponse toResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
}
