package com.example.cinema.model.entity;

import com.example.cinema.model.enums.DiscountType;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "min_benefit_id")
    private MembershipBenefit minTier;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "required_points")
    private Integer requiredPoints = 0;

    @Column(name = "is_redeemable")
    private Boolean isRedeemable = false;

    public Promotion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public MembershipBenefit getMinTier() {
        return minTier;
    }

    public void setMinTier(MembershipBenefit minTier) {
        this.minTier = minTier;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public Integer getRequiredPoints() {
        return requiredPoints;
    }

    public void setRequiredPoints(Integer requiredPoints) {
        this.requiredPoints = requiredPoints;
    }

    public Boolean getIsRedeemable() {
        return isRedeemable;
    }

    public void setIsRedeemable(Boolean redeemable) {
        isRedeemable = redeemable;
    }

    public static class Builder {
        private String code;
        private String name;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private MembershipBenefit minTier;
        private Integer requiredPoints;
        private Boolean isRedeemable;

        public Builder(String code, String name, DiscountType discountType, BigDecimal discountValue) {
            this.code = code;
            this.name = name;
            this.discountType = discountType;
            this.discountValue = discountValue;
        }

        public Builder requiredPoints(Integer points) {
            this.requiredPoints = points;
            return this;
        }

        public Builder redeemable(Boolean redeemable) {
            this.isRedeemable = redeemable;
            return this;
        }

        public Builder validity(LocalDate start, LocalDate end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }

        public Builder minOrder(BigDecimal minOrderAmount) {
            this.minOrderAmount = minOrderAmount;
            return this;
        }

        public Builder maxDiscount(BigDecimal maxDiscountAmount) {
            this.maxDiscountAmount = maxDiscountAmount;
            return this;
        }

        public Builder limit(Integer usageLimit) {
            this.usageLimit = usageLimit;
            return this;
        }

        public Builder minTier(MembershipBenefit minTier) {
            this.minTier = minTier;
            return this;
        }

        public Promotion build() {
            Promotion p = new Promotion();
            p.setCode(this.code);
            p.setName(this.name);
            p.setDiscountType(this.discountType);
            p.setDiscountValue(this.discountValue);
            p.setStartDate(this.startDate);
            p.setEndDate(this.endDate);
            p.setMinOrderAmount(this.minOrderAmount);
            p.setMaxDiscountAmount(this.maxDiscountAmount);
            p.setUsageLimit(this.usageLimit);
            p.setMinTier(this.minTier);
            p.setRequiredPoints(this.requiredPoints != null ? this.requiredPoints : 0);
            p.setIsRedeemable(this.isRedeemable != null ? this.isRedeemable : false);
            p.setIsActive(true);
            return p;
        }
    }
}
