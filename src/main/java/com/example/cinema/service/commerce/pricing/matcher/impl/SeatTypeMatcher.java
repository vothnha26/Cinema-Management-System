package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class SeatTypeMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.SEAT_TYPE; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        return cond.getValue().equals(seat.getSeatType().getId());
    }
}
