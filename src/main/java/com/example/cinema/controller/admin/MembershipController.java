package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.MembershipLevel;
import com.example.cinema.service.user.MembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-benefits")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<List<MembershipLevel>>> getAllLevels() {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.getAllLevels()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipBenefit>>> getAllBenefits() {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.getAllBenefits()));
    }

    @GetMapping("/levels/{levelId}")
    public ResponseEntity<ApiResponse<List<MembershipBenefit>>> getBenefitsByLevel(@PathVariable Long levelId) {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.getBenefitsByLevel(levelId)));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<MembershipBenefit>> updateBenefitRule(
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) String levelName,
            @RequestParam String type,
            @RequestParam String value) {
        
        Long targetId = levelId;
        if (targetId == null && levelName != null) {
            targetId = membershipService.getLevelByName(levelName).getId();
        }
        
        if (targetId == null) {
            throw new com.example.cinema.exception.AppException("Thiếu thông tin hạng thành viên (levelId hoặc levelName)");
        }
        
        return ResponseEntity.ok(ApiResponse.ok(membershipService.updateBenefit(targetId, type, value)));
    }
}
