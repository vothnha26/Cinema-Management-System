package com.example.cinema.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "membership_benefits")
public class MembershipBenefit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membership_level_id", nullable = false)
    private MembershipLevel membershipLevel;

    @Column(name = "benefit_type", nullable = false)
    private String benefitType; // 'DISCOUNT', 'POINT_MULTIPLIER', 'GIFT', etc.

    @Column(name = "benefit_value", nullable = false)
    private String benefitValue;

    public MembershipBenefit() {
    }

    public MembershipBenefit(MembershipLevel level, String type, String value) {
        this.membershipLevel = level;
        this.benefitType = type;
        this.benefitValue = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MembershipLevel getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(MembershipLevel membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public String getBenefitType() {
        return benefitType;
    }

    public void setBenefitType(String benefitType) {
        this.benefitType = benefitType;
    }

    public String getBenefitValue() {
        return benefitValue;
    }

    public void setBenefitValue(String benefitValue) {
        this.benefitValue = benefitValue;
    }

    // Helper methods for common benefits (backward compatibility or convenience)
    public Double getDiscountPercent() {
        if ("DISCOUNT".equals(benefitType)) {
            return Double.valueOf(benefitValue);
        }
        return 0.0;
    }

    public Double getPointMultiplier() {
        if ("POINT_MULTIPLIER".equals(benefitType)) {
            return Double.valueOf(benefitValue);
        }
        return 1.0;
    }
}
