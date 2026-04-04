package com.example.cinema.repository.user;

import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.enums.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MembershipBenefitRepository extends JpaRepository<MembershipBenefit, Long> {
    Optional<MembershipBenefit> findByTier(MembershipTier tier);
}
