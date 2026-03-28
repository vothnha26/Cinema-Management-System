package com.example.cinema.service.pricing;

import java.math.BigDecimal;

public class BasePriceCalculator implements PriceCalculator {
    private final BigDecimal basePrice;

    public BasePriceCalculator(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public BigDecimal calculate() {
        return basePrice;
    }
}
