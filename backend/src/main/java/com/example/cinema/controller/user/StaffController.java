package com.example.cinema.controller.user;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Staff;
import com.example.cinema.model.entity.User;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.repository.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public StaffController(StaffRepository staffRepository, UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getMyInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.ok(ApiResponse.error("Unauthorized", 401));

        Staff staff = staffRepository.findByUserId(user.getId()).orElse(null);
        if (staff == null) return ResponseEntity.ok(ApiResponse.error("Staff profile not found", 404));

        return ResponseEntity.ok(ApiResponse.ok(staff));
    }
}
