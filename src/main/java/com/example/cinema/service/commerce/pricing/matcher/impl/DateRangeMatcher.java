package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateRangeMatcher implements PricingConditionMatcher {
    @Override
    public boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer) {
        String value = condition.getValue();
        if (value == null || !value.contains(":")) return false;

        String[] parts = value.split(":");
        LocalDate start = LocalDate.parse(parts[0].trim());
        LocalDate end = LocalDate.parse(parts[1].trim());

        LocalDate showDate = showtime.getStartTime().toLocalDate();
        return !showDate.isBefore(start) && !showDate.isAfter(end);
    }
}
