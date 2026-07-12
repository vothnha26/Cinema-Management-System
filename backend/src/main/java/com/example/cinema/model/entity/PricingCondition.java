package com.example.cinema.model.entity;

import com.example.cinema.model.enums.PricingConditionType;
import jakarta.persistence.*;

@Entity
@Table(name = "pricing_conditions")
public class PricingCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PricingRule rule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingConditionType type;

    @Column(nullable = false)
    private String value; // Giá trị để so sánh (String hoặc JSON format)

    private String description;

    public PricingCondition() {
    }

    public PricingCondition(PricingRule rule, PricingConditionType type, String value, String description) {
        this.rule = rule;
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PricingRule getRule() {
        return rule;
    }

    public void setRule(PricingRule rule) {
        this.rule = rule;
    }

    public PricingConditionType getType() {
        return type;
    }

    public void setType(PricingConditionType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
