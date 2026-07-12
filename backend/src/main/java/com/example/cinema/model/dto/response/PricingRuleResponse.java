package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.model.enums.PricingRuleCategory;
import java.math.BigDecimal;
import java.util.List;

public class PricingRuleResponse {
    private Long id;
    private String name;
    private String description;
    private PricingRuleCategory category;
    private PricingImpactType impactType;
    private BigDecimal impactValue;
    private BigDecimal maxDiscountLimit;
    private int priority;
    private boolean stackable;
    private boolean active;
    private boolean system;
    private List<ConditionResponse> conditions;

    public static class ConditionResponse {
        private String type;
        private String value;
        private String description;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Getters and Setters
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
    public BigDecimal getMaxDiscountLimit() { return maxDiscountLimit; }
    public void setMaxDiscountLimit(BigDecimal maxDiscountLimit) { this.maxDiscountLimit = maxDiscountLimit; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isSystem() { return system; }
    public void setSystem(boolean system) { this.system = system; }
    public List<ConditionResponse> getConditions() { return conditions; }
    public void setConditions(List<ConditionResponse> conditions) { this.conditions = conditions; }
}
