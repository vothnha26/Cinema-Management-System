package com.example.cinema.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PriceCalculationResult {
    private BigDecimal finalPrice;
    private List<String> appliedRules;

    public PriceCalculationResult(BigDecimal finalPrice, List<String> appliedRules) {
        this.finalPrice = finalPrice;
        this.appliedRules = appliedRules;
    }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public List<String> getAppliedRules() { return appliedRules; }
}
