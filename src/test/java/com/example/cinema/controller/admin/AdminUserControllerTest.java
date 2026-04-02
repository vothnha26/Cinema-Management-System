package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.request.CreateStaffRequest;
import com.example.cinema.model.dto.request.UpdateUserRequest;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // 1. Tạo tài khoản Admin để lấy quyền truy cập (theo Sequence Diagram actor Admin)
        User admin = new User();
        admin.setUsername("admin_tester");
        admin.setEmail("admin_tester@starcinema.vn");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        admin.setStatus(true);
        userRepository.save(admin);
        
        adminToken = "Bearer " + jwtUtil.generateToken(admin, Role.ADMIN.name());
    }

    @Test
    @DisplayName("Admin SD: Tạo mới tài khoản Staff thành công")
    public void testCreateStaff_Success() throws Exception {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setUsername("staff_sd_01");
        request.setPassword("password123");
        request.setEmail("staff_sd_01@starcinema.vn");

        mockMvc.perform(post("/api/admin/users/staff")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("staff_sd_01"))
                .andExpect(jsonPath("$.data.role").value("STAFF"));
    }

    @Test
    @DisplayName("Admin SD: Tạo Staff thất bại do trùng Username (Conflict)")
    public void testCreateStaff_DuplicateUsername() throws Exception {
        // Tạo sẵn một user
        User existingUser = new User();
        existingUser.setUsername("staff_exists");
        existingUser.setEmail("staff@starcinema.vn");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setRole(Role.STAFF);
        existingUser.setStatus(true);
        userRepository.save(existingUser);

        CreateStaffRequest request = new CreateStaffRequest();
        request.setUsername("staff_exists"); // Trùng lặp
        request.setPassword("password123");
        request.setEmail("other@starcinema.vn");

        mockMvc.perform(post("/api/admin/users/staff")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin SD: Lấy danh sách người dùng (Search Users)")
    public void testGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Admin Flow: Cập nhật trạng thái người dùng (Toggle Status)")
    public void testToggleUserStatus_Success() throws Exception {
        User targetUser = new User();
        targetUser.setUsername("toggle_test");
        targetUser.setEmail("toggle@starcinema.vn");
        targetUser.setPassword(passwordEncoder.encode("password123"));
        targetUser.setRole(Role.STAFF);
        targetUser.setStatus(true);
        userRepository.save(targetUser);

        mockMvc.perform(patch("/api/admin/users/" + targetUser.getId() + "/status")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(false));
    }

    @Test
    @DisplayName("Admin Flow: Cập nhật thông tin người dùng")
    public void testUpdateUser_Success() throws Exception {
        User targetUser = new User();
        targetUser.setUsername("update_test");
        targetUser.setEmail("old@starcinema.vn");
        targetUser.setPassword(passwordEncoder.encode("password123"));
        targetUser.setRole(Role.STAFF);
        targetUser.setStatus(true);
        userRepository.save(targetUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new_email@starcinema.vn");
        request.setRole(Role.MANAGER); // Nâng cấp role

        mockMvc.perform(put("/api/admin/users/" + targetUser.getId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("new_email@starcinema.vn"))
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    @DisplayName("Security: Staff không được quyền truy cập API Admin")
    public void testAdminApi_ForbiddenForStaff() throws Exception {
        User staff = new User();
        staff.setUsername("simple_staff");
        staff.setEmail("staff_forbidden@starcinema.vn");
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setRole(Role.STAFF);
        staff.setStatus(true);
        userRepository.save(staff);
        
        String staffToken = "Bearer " + jwtUtil.generateToken(staff, Role.STAFF.name());

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", staffToken))
                .andExpect(status().isForbidden());
    }
}
