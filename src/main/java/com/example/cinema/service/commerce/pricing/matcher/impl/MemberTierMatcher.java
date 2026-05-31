package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class MemberTierMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.MEMBER_TIER; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        if (customer == null || customer.getMembershipLevel() == null) return "GUEST".equals(cond.getValue());
        return cond.getValue().equals(customer.getMembershipLevel().getName());
    }
}
