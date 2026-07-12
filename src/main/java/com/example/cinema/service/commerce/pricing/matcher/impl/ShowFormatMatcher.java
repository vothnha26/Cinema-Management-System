package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class ShowFormatMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.SHOW_FORMAT; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        if (showtime.getFormat() == null) return false;
        return cond.getValue().equals(showtime.getFormat().getName());
    }
}
