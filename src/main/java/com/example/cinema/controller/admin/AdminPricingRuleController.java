package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.PricingRuleResponse;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.service.commerce.PricingRuleService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/pricing-rules")
public class AdminPricingRuleController {

    private final PricingRuleService pricingRuleService;
    private final ModelMapper modelMapper;

    public AdminPricingRuleController(PricingRuleService pricingRuleService, ModelMapper modelMapper) {
        this.pricingRuleService = pricingRuleService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricingRuleResponse>>> getAllRules(@RequestParam(required = false) Long branchId) {
        List<PricingRule> rules;
        if (branchId != null) {
            rules = pricingRuleService.getRulesByBranch(branchId);
        } else {
            rules = pricingRuleService.getAllRules();
        }
        return ResponseEntity.ok(ApiResponse.ok(rules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList())));
    }

    private PricingRuleResponse mapToResponse(PricingRule rule) {
        PricingRuleResponse res = modelMapper.map(rule, PricingRuleResponse.class);
        res.setSystem(rule.isSystem());
        res.setActive(rule.getActive());
        res.setStackable(rule.isStackable());
        
        if (rule.getConditions() != null) {
            res.setConditions(rule.getConditions().stream().map(c -> {
                PricingRuleResponse.ConditionResponse cr = new PricingRuleResponse.ConditionResponse();
                cr.setType(c.getType().name());
                cr.setValue(c.getValue());
                cr.setDescription(c.getDescription());
                return cr;
            }).collect(Collectors.toList()));
        }
        return res;
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
