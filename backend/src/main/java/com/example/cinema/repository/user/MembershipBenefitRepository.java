package com.example.cinema.repository.user;

import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipBenefitRepository extends JpaRepository<MembershipBenefit, Long> {
    List<MembershipBenefit> findByMembershipLevel(MembershipLevel membershipLevel);
    Optional<MembershipBenefit> findByMembershipLevelAndBenefitType(MembershipLevel membershipLevel, String benefitType);
}
