package com.example.cinema.service.commerce.pricing.matcher;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PricingRuleMatcher {
    private final Map<PricingConditionType, IConditionMatcher> matchers;

    public PricingRuleMatcher(List<IConditionMatcher> matcherList) {
        this.matchers = matcherList.stream()
                .collect(Collectors.toMap(IConditionMatcher::getSupportedType, m -> m));
    }

    public boolean matches(PricingRule rule, Showtime showtime, Seat seat, Customer customer) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) return true;
        
        for (PricingCondition cond : rule.getConditions()) {
            IConditionMatcher matcher = matchers.get(cond.getType());
            if (matcher != null) {
                if (!matcher.matches(cond, showtime, seat, customer)) return false;
            } else {
                return false;
            }
        }
        return true;
    }
}
