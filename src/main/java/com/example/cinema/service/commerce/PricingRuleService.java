package com.example.cinema.service.commerce;

import com.example.cinema.model.entity.PricingRule;
import java.util.List;

public interface PricingRuleService {
    List<PricingRule> getAllRules();
    PricingRule getRuleById(Long id);
    PricingRule createRule(PricingRule rule);
    PricingRule updateRule(Long id, PricingRule rule);
    void deleteRule(Long id);
    void reorderRules(Long branchId, List<Long> ruleIds);
}
