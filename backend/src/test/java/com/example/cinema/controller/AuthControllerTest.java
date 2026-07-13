package com.example.cinema.controller.auth;

import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.dto.request.VerifyOtpRequest;
import com.example.cinema.model.dto.response.AuthResponse;
import com.example.cinema.service.auth.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @MockBean
    private com.example.cinema.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.example.cinema.repository.user.UserRepository userRepository;

    @MockBean
    private com.example.cinema.repository.user.CustomerRepository customerRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private VerifyOtpRequest verifyOtpRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Nguyen Van A");
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setUsername("testuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("0987654321");

        verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail("testuser@example.com");
        verifyOtpRequest.setOtp("123456");
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setToken("mock-jwt-token");
        response.setUsername("testuser@example.com");
        response.setRole("CUSTOMER");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser@example.com"));
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn("Đăng ký thành công.");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Đăng ký thành công."));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        doNothing().when(authService).verifyOtp(any(VerifyOtpRequest.class));

        mockMvc.perform(post("/api/auth/verify-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        doNothing().when(authService).requestPasswordReset("testuser@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .param("email", "testuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        doNothing().when(authService).resetPasswordWithToken("123456", "newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .param("token", "123456")
                        .param("newPassword", "newpassword123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
