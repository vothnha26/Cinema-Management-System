package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;

@Component
public class DayOfWeekMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.DAY_OF_WEEK; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        DayOfWeek current = showtime.getStartTime().getDayOfWeek();
        return cond.getValue().contains(current.name());
    }
}
