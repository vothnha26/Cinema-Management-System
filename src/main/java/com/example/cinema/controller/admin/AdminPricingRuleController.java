package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.service.commerce.PricingRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing-rules")
public class AdminPricingRuleController {

    private final PricingRuleService pricingRuleService;

    public AdminPricingRuleController(PricingRuleService pricingRuleService) {
        this.pricingRuleService = pricingRuleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricingRule>>> getAllRules(@RequestParam(required = false) Long branchId) {
        if (branchId != null) {
            return ResponseEntity.ok(ApiResponse.ok(pricingRuleService.getRulesByBranch(branchId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleService.getAllRules()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PricingRule>> createRule(@RequestBody PricingRule rule) {
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleService.createRule(rule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRule>> updateRule(@PathVariable Long id, @RequestBody PricingRule rule) {
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleService.updateRule(id, rule)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        pricingRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderRules(@RequestParam(required = false) Long branchId, @RequestBody List<Long> ruleIds) {
        pricingRuleService.reorderRules(branchId, ruleIds);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
