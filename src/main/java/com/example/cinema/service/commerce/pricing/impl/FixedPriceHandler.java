package com.example.cinema.service.commerce.pricing.impl;

import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.service.commerce.pricing.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class FixedPriceHandler implements IPriceModifierHandler {
    @Override public boolean canHandle(PricingRule rule) { return rule.getImpactType() == PricingImpactType.FIXED; }
    @Override public PriceCalculator handle(PriceCalculator current, PricingRule rule) {
        final BigDecimal fixed = rule.getImpactValue();
        return () -> fixed;
    }
}
