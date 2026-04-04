package com.example.cinema.model.entity;

import com.example.cinema.model.enums.MembershipTier;
import jakarta.persistence.*;

@Entity
@Table(name = "membership_benefits")
public class MembershipBenefit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private MembershipTier tier;

    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent; // Ví dụ: 5.0, 10.0

    @Column(name = "point_multiplier")
    private Double pointMultiplier = 1.0; // Hệ số tích điểm (Ví dụ hạng cao tích điểm nhanh hơn)

    public MembershipBenefit() {
    }

    public MembershipBenefit(MembershipTier tier, Double discountPercent) {
        this.tier = tier;
        this.discountPercent = discountPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Double getPointMultiplier() {
        return pointMultiplier;
    }

    public void setPointMultiplier(Double pointMultiplier) {
        this.pointMultiplier = pointMultiplier;
    }
}
