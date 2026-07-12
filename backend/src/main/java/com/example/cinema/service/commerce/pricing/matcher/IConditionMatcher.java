package com.example.cinema.service.commerce.pricing.matcher;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;

public interface IConditionMatcher {
    PricingConditionType getSupportedType();
    boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer);
}
