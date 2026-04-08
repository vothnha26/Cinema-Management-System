package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class RoomTypeMatcher implements PricingConditionMatcher {
    @Override
    public boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer) {
        String value = condition.getValue();
        if (value == null || value.isEmpty()) return false;

        String showRoomTypeId = String.valueOf(showtime.getRoom().getRoomType().getId());
        return value.equals(showRoomTypeId);
    }
}
