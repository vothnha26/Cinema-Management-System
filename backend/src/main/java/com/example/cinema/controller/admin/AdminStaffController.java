package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.Staff;
import com.example.cinema.model.entity.User;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/staffs")
public class AdminStaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Staff>>> getAllStaffs() {
        return ResponseEntity.ok(ApiResponse.ok(staffRepository.findAll()));
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<Long, Staff>>> getStaffAssignments() {
        List<Staff> staffs = staffRepository.findAll();
        Map<Long, Staff> map = new HashMap<>();
        for (Staff s : staffs) {
            map.put(s.getUser().getId(), s);
        }
        return ResponseEntity.ok(ApiResponse.ok(map));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Staff>> assignStaffToBranch(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long branchId = Long.valueOf(payload.get("branchId").toString());
        String staffCode = payload.get("staffCode").toString();
        String position = payload.get("position").toString();

        User user = userRepository.findById(userId).orElseThrow();
        Branch branch = branchRepository.findById(branchId).orElseThrow();

        Staff staff = staffRepository.findByUser(user).orElse(new Staff());
        staff.setUser(user);
        staff.setBranch(branch);
        staff.setStaffCode(staffCode);
        staff.setPosition(position);

        return ResponseEntity.ok(ApiResponse.ok(staffRepository.save(staff)));
    }
}
