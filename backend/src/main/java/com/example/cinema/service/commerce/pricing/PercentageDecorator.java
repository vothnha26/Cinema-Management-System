package com.example.cinema.service.commerce.pricing;

import java.math.BigDecimal;

public class PercentageDecorator extends PriceDecorator {
    private final BigDecimal multiplier;

    public PercentageDecorator(PriceCalculator wrapped, BigDecimal multiplier) {
        super(wrapped);
        this.multiplier = multiplier;
    }

    @Override
    public BigDecimal calculate() {
        return super.calculate().multiply(multiplier);
    }
}
