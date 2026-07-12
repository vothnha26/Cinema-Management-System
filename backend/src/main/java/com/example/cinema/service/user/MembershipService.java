package com.example.cinema.service.user;

import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.MembershipLevel;
import java.util.List;

public interface MembershipService {
    List<MembershipLevel> getAllLevels();
    MembershipLevel getLevelByName(String name);
    
    List<MembershipBenefit> getAllBenefits();
    List<MembershipBenefit> getBenefitsByLevel(Long levelId);
    
    MembershipBenefit updateBenefit(Long levelId, String type, String value);
}
