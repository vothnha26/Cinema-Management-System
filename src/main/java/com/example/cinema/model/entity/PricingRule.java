package com.example.cinema.model.entity;

import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.model.enums.PricingRuleCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pricing_rules")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private PricingRuleCategory category; // BASE, SURCHARGE, DISCOUNT

    @Enumerated(EnumType.STRING)
    private PricingImpactType impactType; // ADDITIVE, PERCENTAGE, FIXED

    private BigDecimal impactValue; // Giá trị cộng thêm hoặc tỉ lệ nhân

    private int priority = 100; // Thứ tự ưu tiên (thấp hơn chạy trước)

    private boolean isStackable = true; // Có được cộng dồn với các rule khác không?

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PricingCondition> conditions = new ArrayList<>();

    public PricingRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PricingRuleCategory getCategory() {
        return category;
    }

    public void setCategory(PricingRuleCategory category) {
        this.category = category;
    }

    public PricingImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(PricingImpactType impactType) {
        this.impactType = impactType;
    }

    public BigDecimal getImpactValue() {
        return impactValue;
    }

    public void setImpactValue(BigDecimal impactValue) {
        this.impactValue = impactValue;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public void setStackable(boolean stackable) {
        isStackable = stackable;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<PricingCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<PricingCondition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(PricingCondition condition) {
        conditions.add(condition);
        condition.setRule(this);
    }

    public void removeCondition(PricingCondition condition) {
        conditions.remove(condition);
        condition.setRule(null);
    }
}
