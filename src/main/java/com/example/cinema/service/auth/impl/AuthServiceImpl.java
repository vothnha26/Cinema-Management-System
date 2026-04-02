package com.example.cinema.service.auth.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.exception.DuplicateResourceException;
import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.auth.IAuthService;
import com.example.cinema.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation xác thực (SRP: Chỉ xử lý logic đăng nhập/đăng ký).
 * Không chứa logic quản lý user hay phân quyền.
 */
@Service
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException("Tên đăng nhập hoặc mật khẩu không chính xác.", 401);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("Người dùng không tồn tại.", 404));

        if (!user.getStatus()) {
            throw new AppException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.", 403);
        }

        String role = user.getRole().name();
        String token = jwtUtil.generateToken(userDetails, role);

        return new AuthResponse(token, role, user.getUsername());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra trùng lặp
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        // Tạo User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);
        userRepository.save(user);

        // Tạo Customer profile song song
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setMembershipTier(MembershipTier.STANDARD);
        customer.setTotalSpending(BigDecimal.ZERO);
        customer.setPoints(0);
        customerRepository.save(customer);

        // Tự động login sau khi đăng ký
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        return new AuthResponse(token, user.getRole().name(), user.getUsername());
    }
}
