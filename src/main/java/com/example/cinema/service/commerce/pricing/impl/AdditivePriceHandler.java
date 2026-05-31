package com.example.cinema.service.commerce.pricing.impl;

import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.service.commerce.pricing.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AdditivePriceHandler implements IPriceModifierHandler {
    @Override public boolean canHandle(PricingRule rule) { return rule.getImpactType() == PricingImpactType.ADDITIVE || rule.getImpactType() == PricingImpactType.SUBTRACTIVE; }
    @Override public PriceCalculator handle(PriceCalculator current, PricingRule rule) {
        BigDecimal val = rule.getImpactType() == PricingImpactType.SUBTRACTIVE ? rule.getImpactValue().negate() : rule.getImpactValue();
        return new AdditiveDecorator(current, val);
    }
}
