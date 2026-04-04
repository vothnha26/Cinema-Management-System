package com.example.cinema.service.user.impl;

import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.service.user.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final MembershipBenefitRepository benefitRepository;

    public MembershipServiceImpl(MembershipBenefitRepository benefitRepository) {
        this.benefitRepository = benefitRepository;
    }

    @Override
    public List<MembershipBenefit> getAllBenefits() {
        return benefitRepository.findAll();
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "UPDATE", target = "MEMBERSHIP_BENEFIT")
    public MembershipBenefit updateBenefit(MembershipTier tier, Double discountPercent, Double pointMultiplier) {
        MembershipBenefit benefit = benefitRepository.findByTier(tier)
                .orElseThrow(() -> new com.example.cinema.exception.ResourceNotFoundException("Membership Benefit", "tier", tier.name()));

        benefit.setDiscountPercent(discountPercent);
        benefit.setPointMultiplier(pointMultiplier);
        return benefitRepository.save(benefit);
    }
}
