package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.BranchPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchPricingRuleRepository extends JpaRepository<BranchPricingRule, Long> {
    
    @Query("SELECT bpr FROM BranchPricingRule bpr WHERE bpr.branch.id = :branchId ORDER BY bpr.priority ASC")
    List<BranchPricingRule> findAllByBranchIdOrderByPriorityAsc(@Param("branchId") Long branchId);

    void deleteByRuleId(Long ruleId);
}
