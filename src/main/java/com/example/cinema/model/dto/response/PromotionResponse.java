package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.MembershipTier;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionResponse {
    private Long id;
    private String code;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private MembershipTier minTier;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer requiredPoints;
    private Boolean isRedeemable;
    private BigDecimal appliedDiscountAmount;

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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

    public BigDecimal getAppliedDiscountAmount() {
        return appliedDiscountAmount;
    }

    public void setAppliedDiscountAmount(BigDecimal appliedDiscountAmount) {
        this.appliedDiscountAmount = appliedDiscountAmount;
    }
}
