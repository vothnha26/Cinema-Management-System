package com.example.cinema.model.entity;

import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.MembershipTier;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "min_tier")
    private MembershipTier minTier = MembershipTier.STANDARD;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Promotion() {}

    // Builder Class
    public static class Builder {
        private Promotion promotion = new Promotion();

        public Builder(String code, String name, DiscountType type, BigDecimal value) {
            promotion.code = code;
            promotion.name = name;
            promotion.discountType = type;
            promotion.discountValue = value;
        }

        public Builder validity(LocalDate start, LocalDate end) {
            promotion.startDate = start;
            promotion.endDate = end;
            return this;
        }

        public Builder minOrder(BigDecimal amount) {
            promotion.minOrderAmount = amount;
            return this;
        }

        public Builder maxDiscount(BigDecimal amount) {
            promotion.maxDiscountAmount = amount;
            return this;
        }

        public Builder limit(Integer limit) {
            promotion.usageLimit = limit;
            return this;
        }

        public Builder minTier(MembershipTier tier) {
            promotion.minTier = tier;
            return this;
        }

        public Promotion build() {
            if (promotion.usedCount == null) promotion.usedCount = 0;
            if (promotion.isActive == null) promotion.isActive = true;
            return promotion;
        }
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public MembershipTier getMinTier() { return minTier; }
    public void setMinTier(MembershipTier minTier) { this.minTier = minTier; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
