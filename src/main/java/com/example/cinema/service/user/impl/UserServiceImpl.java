package com.example.cinema.service.user.impl;

import com.example.cinema.exception.DuplicateResourceException;
import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.dto.response.UserResponse;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.UserRepository;
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

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
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
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(true);

        return toResponse(userRepository.save(user));
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

    private UserResponse toResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
}
