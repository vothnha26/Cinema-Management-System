package com.example.cinema.controller.auth;

import com.example.cinema.model.dto.request.LoginRequest;
import com.example.cinema.model.dto.request.RegisterRequest;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user_123");
        request.setPassword("password123");
        request.setEmail("new_user@starcinema.vn");
        request.setFullName("New User");
        request.setPhone("0999888777");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("new_user_123"));
    }

    @Test
    public void testRegister_DuplicateUsername() throws Exception {
        // Tạo sẵn user
        User user = new User();
        user.setUsername("existing_user");
        user.setEmail("unique@starcinema.vn");
        user.setPassword(passwordEncoder.encode("123"));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);
        userRepository.save(user);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing_user"); // Trùng
        request.setPassword("password123");
        request.setEmail("other@starcinema.vn");
        request.setFullName("Existing User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testRegister_DuplicateEmail() throws Exception {
        User user = new User();
        user.setUsername("unique_user");
        user.setEmail("duplicate_email@starcinema.vn");
        user.setPassword(passwordEncoder.encode("123"));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);
        userRepository.save(user);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user");
        request.setPassword("password123");
        request.setEmail("duplicate_email@starcinema.vn"); // Trùng
        request.setFullName("Duplicate Email");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testLogin_Success() throws Exception {
        User user = new User();
        user.setUsername("login_test_user");
        user.setEmail("login@starcinema.vn");
        user.setPassword(passwordEncoder.encode("secret_pass"));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("login_test_user");
        request.setPassword("secret_pass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    public void testLogin_WrongPassword() throws Exception {
        User user = new User();
        user.setUsername("wrong_pass_user");
        user.setEmail("wrong_pass@starcinema.vn");
        user.setPassword(passwordEncoder.encode("correct_pass"));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("wrong_pass_user");
        request.setPassword("wrong_pass"); // Sai mật khẩu

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testLogin_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("non_existent_user");
        request.setPassword("any_pass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // Do AuthenticationManager ném BadCredentialsException
    }

    @Test
    public void testLogin_AccountLocked() throws Exception {
        User user = new User();
        user.setUsername("locked_user");
        user.setEmail("locked@starcinema.vn");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.CUSTOMER);
        user.setStatus(false); // Bị khóa
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("locked_user");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."));
    }
}
