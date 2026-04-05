package com.example.cinema.service.auth.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.security.JwtUtil;
import com.example.cinema.security.UserDetailsServiceImpl;
import com.example.cinema.service.auth.IAuthService;
import com.example.cinema.service.notification.INotificationService;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final INotificationService notificationService;

    private static final String RESET_TOKEN_PREFIX = "reset_token:";

    public AuthServiceImpl(UserRepository userRepository, CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService,
            StringRedisTemplate redisTemplate, INotificationService notificationService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.CUSTOMER);
        user = userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setPoints(0);
        customer.setMembershipTier(MembershipTier.STANDARD);
        customer.setTotalSpending(BigDecimal.ZERO);
        customerRepository.save(customer);

        // Auto login after registration
        return login(new LoginRequest(request.getUsername(), request.getPassword()));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate using the provided identifier (username or email)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Retrieve user by username OR email for the response
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new AppException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwt = jwtUtil.generateToken(userDetails, user.getRole().name());
        String fullName = user.getUsername();

        if (user.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
            if (customer != null)
                fullName = customer.getFullName();
        }

        return new AuthResponse(jwt, user.getUsername(), user.getRole().name(), fullName);
    }

    @Override
    @Transactional
    public void changePassword(String username, com.example.cinema.model.dto.request.ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với email này"));

        // 1. Sinh Token duy nhất
        String token = UUID.randomUUID().toString();

        // 2. Lưu vào Redis (Hết hạn sau 15 phút)
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + token, email, Duration.ofMinutes(15));

        // 3. Gửi Email qua Notification System
        String resetLink = "http://localhost:8082/reset-password.html?token=" + token;
        String subject = "🔑 Yêu cầu đặt lại mật khẩu - StarCinema";
        String body = "<p>Chào bạn,</p>"
                + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản StarCinema của bạn.</p>"
                + "<p>Vui lòng nhấn vào nút bên dưới để thực hiện thay đổi mật khẩu (Link có hiệu lực trong 15 phút):</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "  <a href='" + resetLink + "' style='background-color: #E5133A; color: white; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 5px;'>ĐẶT LẠI MẬT KHẨU</a>"
                + "</div>"
                + "<p>Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.</p>";

        notificationService.sendNotification(email, subject, body, "EMAIL");
    }

    @Override
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        // 1. Kiểm tra token trong Redis
        String email = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + token);
        if (email == null) {
            throw new AppException("Link đã hết hạn hoặc không hợp lệ. Vui lòng yêu cầu lại.");
        }

        // 2. Cập nhật mật khẩu
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 3. Xóa token ngay sau khi dùng xong (One-time use)
        redisTemplate.delete(RESET_TOKEN_PREFIX + token);
    }
}
