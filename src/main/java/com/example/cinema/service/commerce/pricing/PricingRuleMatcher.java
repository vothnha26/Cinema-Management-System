package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcherRegistry;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleMatcher {

    private final PricingConditionMatcherRegistry matcherRegistry;

    public PricingRuleMatcher(PricingConditionMatcherRegistry matcherRegistry) {
        this.matcherRegistry = matcherRegistry;
    }

    public boolean matches(PricingRule rule, Showtime showtime, Seat seat, com.example.cinema.model.entity.Customer customer) {
        if (!rule.isActive()) return false;

        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return true;
        }

        for (com.example.cinema.model.entity.PricingCondition condition : rule.getConditions()) {
            PricingConditionMatcher matcher = matcherRegistry.getMatcher(condition.getType());
            if (matcher != null) {
                if (!matcher.matches(condition, showtime, seat, customer)) {
                    return false;
                }
            }
        }

        return true;
    }
}
