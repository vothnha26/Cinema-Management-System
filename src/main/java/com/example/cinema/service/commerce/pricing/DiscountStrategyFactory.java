package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DiscountStrategyFactory {
    private final Map<DiscountType, DiscountStrategy> strategies = new EnumMap<>(DiscountType.class);

    public DiscountStrategyFactory() {
        strategies.put(DiscountType.PERCENT, new PercentageDiscountStrategy());
        strategies.put(DiscountType.FIXED, new FixedDiscountStrategy());
    }

    public DiscountStrategy getStrategy(DiscountType type) {
        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for discount type: " + type);
        }
        return strategy;
    }
}
