package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.MembershipTier;
<<<<<<< HEAD
=======

>>>>>>> feature/Customer
import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionResponse {
<<<<<<< HEAD
    private Long id;
=======
>>>>>>> feature/Customer
    private String code;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
<<<<<<< HEAD
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private MembershipTier minTier;
    private Boolean isActive;
    private BigDecimal appliedDiscountAmount;

    public PromotionResponse() {}

    // Getters/Setters
    public BigDecimal getAppliedDiscountAmount() { return appliedDiscountAmount; }
    public void setAppliedDiscountAmount(BigDecimal appliedDiscountAmount) { this.appliedDiscountAmount = appliedDiscountAmount; }

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
    public MembershipTier getMinTier() { return minTier; }
    public void setMinTier(MembershipTier minTier) { this.minTier = minTier; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
=======
    private MembershipTier minTier;
    private LocalDate startDate;
    private LocalDate endDate;

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

    public MembershipTier getMinTier() {
        return minTier;
    }

    public void setMinTier(MembershipTier minTier) {
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
>>>>>>> feature/Customer
}
