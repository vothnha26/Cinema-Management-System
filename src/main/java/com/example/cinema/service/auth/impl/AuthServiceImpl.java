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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthServiceImpl(UserRepository userRepository, CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với email này"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
