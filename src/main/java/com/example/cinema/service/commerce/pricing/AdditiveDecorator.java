package com.example.cinema.service.commerce.pricing;

import java.math.BigDecimal;

public class AdditiveDecorator extends PriceDecorator {
    private final BigDecimal surcharge;

    public AdditiveDecorator(PriceCalculator wrapped, BigDecimal surcharge) {
        super(wrapped);
        this.surcharge = surcharge;
    }

    @Override
    public BigDecimal calculate() {
        return super.calculate().add(surcharge);
    }
}
