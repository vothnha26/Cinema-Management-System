package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TimeRangeMatcher implements PricingConditionMatcher {
    @Override
    public boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer) {
        String value = condition.getValue();
        if (value == null || !value.contains("-")) return false;

        String[] parts = value.split("-");
        LocalTime start = LocalTime.parse(parts[0].trim());
        LocalTime end = LocalTime.parse(parts[1].trim());

        LocalTime showTime = showtime.getStartTime().toLocalTime();
        return !showTime.isBefore(start) && !showTime.isAfter(end);
    }
}
