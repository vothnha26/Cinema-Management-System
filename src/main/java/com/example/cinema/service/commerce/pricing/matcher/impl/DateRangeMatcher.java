package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.IConditionMatcher;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DateRangeMatcher implements IConditionMatcher {
    @Override public PricingConditionType getSupportedType() { return PricingConditionType.DATE_RANGE; }
    @Override public boolean matches(PricingCondition cond, Showtime showtime, Seat seat, Customer customer) {
        String[] range = cond.getValue().split(",");
        if (range.length != 2) return false;
        LocalDate start = LocalDate.parse(range[0]);
        LocalDate end = LocalDate.parse(range[1]);
        LocalDate current = showtime.getStartTime().toLocalDate();
        return !current.isBefore(start) && !current.isAfter(end);
    }
}
