package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;
import java.time.LocalTime;

@Component
public class TimeRangeMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.TIME_RANGE; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        String[] range = cond.getValue().split("-");
        if (range.length != 2) return false;
        LocalTime start = LocalTime.parse(range[0]);
        LocalTime end = LocalTime.parse(range[1]);
        LocalTime current = showtime.getStartTime().toLocalTime();
        return !current.isBefore(start) && !current.isAfter(end);
    }
}
