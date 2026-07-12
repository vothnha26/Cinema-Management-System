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
    private PricingRuleCategory category;

    @Enumerated(EnumType.STRING)
    private PricingImpactType impactType;

    private BigDecimal impactValue;

    private int priority = 100;

    @Column(name = "is_stackable")
    private Boolean stackable = true;

    @Column(name = "max_discount_limit")
    private BigDecimal maxDiscountLimit;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "is_system", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean systemRule = false;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PricingCondition> conditions = new ArrayList<>();

    public PricingRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PricingRuleCategory getCategory() { return category; }
    public void setCategory(PricingRuleCategory category) { this.category = category; }
    public PricingImpactType getImpactType() { return impactType; }
    public void setImpactType(PricingImpactType impactType) { this.impactType = impactType; }
    public BigDecimal getImpactValue() { return impactValue; }
    public void setImpactValue(BigDecimal impactValue) { this.impactValue = impactValue; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    // Boolean wrapper getters/setters
    public Boolean getStackable() { return stackable != null && stackable; }
    public void setStackable(Boolean stackable) { this.stackable = stackable; }
    public Boolean getActive() { return active != null && active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getSystemRule() { return systemRule != null && systemRule; }
    public void setSystemRule(Boolean systemRule) { this.systemRule = systemRule; }

    // Tiện ích để ModelMapper/Logic không bị NPE
    public boolean isSystem() { return systemRule != null && systemRule; }
    public boolean isActive() { return active != null && active; }
    public boolean isStackable() { return stackable != null && stackable; }

    public BigDecimal getMaxDiscountLimit() { return maxDiscountLimit; }
    public void setMaxDiscountLimit(BigDecimal maxDiscountLimit) { this.maxDiscountLimit = maxDiscountLimit; }
    public List<PricingCondition> getConditions() { return conditions; }
    public void setConditions(List<PricingCondition> conditions) { this.conditions = conditions; }
    public void addCondition(PricingCondition condition) { conditions.add(condition); condition.setRule(this); }
    public void removeCondition(PricingCondition condition) { conditions.remove(condition); condition.setRule(null); }
}
