package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.enums.MembershipTier;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipBenefit>>> getAllBenefits() {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.getAllBenefits()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MembershipBenefit>> createBenefit(
            @RequestParam MembershipTier tier,
            @RequestParam Double discountPercent,
            @RequestParam Double pointMultiplier) {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.updateBenefit(tier, discountPercent, pointMultiplier)));
    }

    @PutMapping("/{tier}")
    public ResponseEntity<ApiResponse<MembershipBenefit>> updateBenefit(
            @PathVariable MembershipTier tier,
            @RequestParam Double discountPercent,
            @RequestParam Double pointMultiplier) {
        return ResponseEntity.ok(ApiResponse.ok(membershipService.updateBenefit(tier, discountPercent, pointMultiplier)));
    }
}
