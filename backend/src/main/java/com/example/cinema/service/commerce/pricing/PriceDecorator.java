package com.example.cinema.service.commerce.pricing;

import java.math.BigDecimal;

public abstract class PriceDecorator implements PriceCalculator {
    protected PriceCalculator wrapped;

    public PriceDecorator(PriceCalculator wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculate() {
        return wrapped.calculate();
    }
}
