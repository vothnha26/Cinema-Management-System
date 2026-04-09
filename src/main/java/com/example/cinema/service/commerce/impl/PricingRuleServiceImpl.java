package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.entity.BranchPricingRule;
import com.example.cinema.repository.commerce.PricingRuleRepository;
import com.example.cinema.repository.commerce.BranchPricingRuleRepository;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.service.commerce.PricingRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final BranchRepository branchRepository;
    private final BranchPricingRuleRepository branchPricingRuleRepository;

    public PricingRuleServiceImpl(PricingRuleRepository pricingRuleRepository,
                                  BranchRepository branchRepository,
                                  BranchPricingRuleRepository branchPricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.branchRepository = branchRepository;
        this.branchPricingRuleRepository = branchPricingRuleRepository;
    }

    @Override
    public List<PricingRule> getAllRules() {
        return pricingRuleRepository.findAll();
    }

    @Override
    public List<PricingRule> getRulesByBranch(Long branchId) {
        return branchPricingRuleRepository.findAllByBranchIdOrderByPriorityAsc(branchId).stream()
                .map(BranchPricingRule::getRule)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PricingRule getRuleById(Long id) {
        return pricingRuleRepository.findById(id)
                .orElseThrow(() -> new com.example.cinema.exception.ResourceNotFoundException("PricingRule", id));
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "PRICING_RULE")
    public PricingRule updateRule(Long id, PricingRule rule) {
        if (!pricingRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("PricingRule", id);
        }
        rule.setId(id);
        linkConditions(rule);
        return pricingRuleRepository.save(rule);
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "PRICING_RULE")
    public PricingRule createRule(PricingRule rule) {
        linkConditions(rule);
        PricingRule savedRule = pricingRuleRepository.save(rule);
        
        Long targetBranchId = null;
        if (rule.getConditions() != null) {
            for (com.example.cinema.model.entity.PricingCondition c : rule.getConditions()) {
                if ("BRANCH".equals(c.getType().name())) {
                    targetBranchId = Long.valueOf(c.getValue());
                    break;
                }
            }
        }

        if (targetBranchId != null) {
            createBranchLink(targetBranchId, savedRule);
        } else {
            List<com.example.cinema.model.entity.Branch> branches = branchRepository.findAll();
            for (com.example.cinema.model.entity.Branch b : branches) {
                createBranchLink(b.getId(), savedRule);
            }
        }
        return savedRule;
    }

    private void createBranchLink(Long branchId, PricingRule rule) {
        com.example.cinema.model.entity.Branch branch = branchRepository.findById(branchId).orElse(null);
        if (branch != null) {
            BranchPricingRule link = new BranchPricingRule();
            link.setBranch(branch);
            link.setRule(rule);
            List<BranchPricingRule> existing = branchPricingRuleRepository.findAllByBranchIdOrderByPriorityAsc(branchId);
            link.setPriority(existing.size() + 1);
            branchPricingRuleRepository.save(link);
        }
    }

    private void linkConditions(PricingRule rule) {
        if (rule.getConditions() != null) {
            for (com.example.cinema.model.entity.PricingCondition condition : rule.getConditions()) {
                condition.setRule(rule);
            }
        }
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "PRICING_RULE")
    public void deleteRule(Long id) {
        if (!pricingRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("PricingRule", id);
        }
        branchPricingRuleRepository.deleteByRuleId(id);
        pricingRuleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void reorderRules(Long branchId, List<Long> ruleIds) {
        if (branchId != null) {
            // Trường hợp Manager reorder thứ tự tại chi nhánh
            List<BranchPricingRule> existingLinks = branchPricingRuleRepository.findAllByBranchIdOrderByPriorityAsc(branchId);
            for (int i = 0; i < ruleIds.size(); i++) {
                final Long rid = ruleIds.get(i);
                final int priority = i + 1;
                existingLinks.stream()
                    .filter(l -> l.getRule().getId().equals(rid))
                    .findFirst()
                    .ifPresent(link -> {
                        link.setPriority(priority);
                        branchPricingRuleRepository.save(link);
                    });
            }
        } else {
            // Trường hợp Admin reorder thứ tự mặc định của Hệ thống
            for (int i = 0; i < ruleIds.size(); i++) {
                final Long rid = ruleIds.get(i);
                final int priority = i + 1;
                pricingRuleRepository.findById(rid).ifPresent(rule -> {
                    rule.setPriority(priority);
                    pricingRuleRepository.save(rule);
                });
            }
        }
    }
}
