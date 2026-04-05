package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.repository.commerce.PricingRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing-rules")
public class AdminPricingRuleController {

    private final PricingRuleRepository pricingRuleRepository;

    public AdminPricingRuleController(PricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricingRule>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleRepository.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PricingRule>> createRule(@RequestBody PricingRule rule) {
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleRepository.save(rule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRule>> updateRule(@PathVariable Long id, @RequestBody PricingRule rule) {
        rule.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(pricingRuleRepository.save(rule)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        pricingRuleRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
