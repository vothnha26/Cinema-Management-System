package com.example.cinema.service.commerce.pricing;

import java.math.BigDecimal;

public abstract class PriceDecorator implements PriceCalculator {
    protected PriceCalculator wrappedCalculator;

    public PriceDecorator(PriceCalculator calculator) {
        this.wrappedCalculator = calculator;
    }

    @Override
    public abstract BigDecimal calculate();
}
