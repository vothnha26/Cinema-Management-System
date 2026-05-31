package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class BranchMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.BRANCH; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        return cond.getValue().equals(showtime.getRoom().getBranch().getId().toString());
    }
}
