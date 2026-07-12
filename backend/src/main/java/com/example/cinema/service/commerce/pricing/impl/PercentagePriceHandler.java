package com.example.cinema.service.commerce.pricing.impl;

import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.service.commerce.pricing.*;
import org.springframework.stereotype.Component;

@Component
public class PercentagePriceHandler implements IPriceModifierHandler {
    @Override public boolean canHandle(PricingRule rule) { return rule.getImpactType() == PricingImpactType.PERCENTAGE; }
    @Override public PriceCalculator handle(PriceCalculator current, PricingRule rule) {
        return new PercentageDecorator(current, rule.getImpactValue());
    }
}
