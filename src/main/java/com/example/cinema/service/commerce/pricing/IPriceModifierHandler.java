package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.entity.PricingRule;
import java.math.BigDecimal;

public interface IPriceModifierHandler {
    boolean canHandle(PricingRule rule);
    PriceCalculator handle(PriceCalculator currentCalculator, PricingRule rule);
}
