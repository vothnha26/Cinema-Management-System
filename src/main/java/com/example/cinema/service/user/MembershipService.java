package com.example.cinema.service.user;

import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.enums.MembershipTier;
import java.util.List;

public interface MembershipService {
    List<MembershipBenefit> getAllBenefits();
    MembershipBenefit updateBenefit(MembershipTier tier, Double discountPercent, Double pointMultiplier);
}
