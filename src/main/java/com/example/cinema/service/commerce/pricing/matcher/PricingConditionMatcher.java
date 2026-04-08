package com.example.cinema.service.commerce.pricing.matcher;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;

public interface PricingConditionMatcher {
    boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer);
}
