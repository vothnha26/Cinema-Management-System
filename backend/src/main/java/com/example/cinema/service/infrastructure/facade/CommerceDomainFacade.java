package com.example.cinema.service.infrastructure.facade;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.commerce.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class CommerceDomainFacade {
    private final ComboRepository comboRepo;
    private final BranchComboRepository branchComboRepo;
    private final PromotionRepository promotionRepo;
    private final BranchPricingRuleRepository branchPricingRuleRepo;

    public CommerceDomainFacade(ComboRepository comboRepo, BranchComboRepository branchComboRepo, 
                               PromotionRepository promotionRepo, BranchPricingRuleRepository branchPricingRuleRepo) {
        this.comboRepo = comboRepo;
        this.branchComboRepo = branchComboRepo;
        this.promotionRepo = promotionRepo;
        this.branchPricingRuleRepo = branchPricingRuleRepo;
    }

    public List<Combo> findAllCombos() { return comboRepo.findAll(); }
    public Optional<Combo> findCombo(Long id) { return comboRepo.findById(id); }
    public Combo saveCombo(Combo c) { return comboRepo.save(c); }
    
    public Optional<BranchCombo> findBranchCombo(Branch b, Combo c) { return branchComboRepo.findByBranchAndCombo(b, c); }
    public List<BranchCombo> findCombosByBranch(Long bId) { return branchComboRepo.findAllByBranchId(bId); }
    public void saveBranchCombo(BranchCombo bc) { branchComboRepo.save(bc); }
    
    public Optional<Promotion> findPromotionByCode(String code) { return promotionRepo.findByCodeAndIsActiveTrue(code); }
    public List<BranchPricingRule> findRulesByBranch(Long bId) { return branchPricingRuleRepo.findAllByBranchIdOrderByPriorityAsc(bId); }
}
