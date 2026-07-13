package com.example.cinema.service.auth.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.*;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.Role;
import com.example.cinema.security.JwtUtil;
import com.example.cinema.service.auth.IAuthService;
import com.example.cinema.service.infrastructure.facade.UserDomainFacade;
import com.example.cinema.model.constant.AppConstants;
import com.example.cinema.model.constant.ErrorMessages;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cinema.repository.user.VerificationCodeRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserDomainFacade userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeRepository verificationCodeRepository;

    public AuthServiceImpl(UserDomainFacade userRepo, AuthenticationManager authenticationManager, 
                           JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                           VerificationCodeRepository verificationCodeRepository) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorMessages.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorMessages.INVALID_CREDENTIALS);
        }
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            String code = generateOtp();
            createVerificationCode(user, AppConstants.PURPOSE_REGISTER, code);
            throw new AppException(ErrorMessages.EMAIL_NOT_VERIFIED);
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken(user, user.getRole().name());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        return response;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepo.findUserByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorMessages.USERNAME_ALREADY_EXISTS);
        }
        if (userRepo.findUserByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorMessages.EMAIL_ALREADY_IN_USE);
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(false);
        
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        userRepo.saveCustomer(customer);

        String code = generateOtp();
        createVerificationCode(user, AppConstants.PURPOSE_REGISTER, code);

        return ErrorMessages.REGISTRATION_SUCCESS;
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        User user = userRepo.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorMessages.EMAIL_NOT_FOUND));
        
        Optional<VerificationCode> optCode = verificationCodeRepository
                .findTopByUserAndCodeAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        user, request.getOtp(), AppConstants.PURPOSE_REGISTER, LocalDateTime.now());
        
        if (optCode.isEmpty()) {
            optCode = verificationCodeRepository
                    .findTopByUserAndCodeAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                            user, request.getOtp(), AppConstants.PURPOSE_RESET_PASSWORD, LocalDateTime.now());
        }

        if (optCode.isEmpty()) {
            throw new AppException(ErrorMessages.INVALID_OR_EXPIRED_OTP);
        }

        VerificationCode vc = optCode.get();
        if (AppConstants.PURPOSE_REGISTER.equals(vc.getPurpose())) {
            user.setEmailVerified(true);
            userRepo.saveUser(user);
            vc.setUsedAt(LocalDateTime.now());
            verificationCodeRepository.save(vc);
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorMessages.INCORRECT_OLD_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.saveUser(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorMessages.EMAIL_NOT_FOUND));
        
        String code = generateOtp();
        createVerificationCode(user, AppConstants.PURPOSE_RESET_PASSWORD, code);
    }

    @Override
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        VerificationCode vc = verificationCodeRepository
                .findTopByCodeAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        token, AppConstants.PURPOSE_RESET_PASSWORD, LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorMessages.INVALID_OR_EXPIRED_OTP));

        User user = vc.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.saveUser(user);

        vc.setUsedAt(LocalDateTime.now());
        verificationCodeRepository.save(vc);
    }

    private String generateOtp() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void createVerificationCode(User user, String purpose, String code) {
        VerificationCode vc = new VerificationCode();
        vc.setUser(user);
        vc.setCode(code);
        vc.setPurpose(purpose);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        verificationCodeRepository.save(vc);
        System.out.println(">>> [OTP SIMULATION] Generated OTP for user " + user.getEmail() 
                + " [Purpose: " + purpose + "]: " + code);
    }
}
