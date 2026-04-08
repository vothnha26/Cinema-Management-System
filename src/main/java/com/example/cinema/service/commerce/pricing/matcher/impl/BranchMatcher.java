package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import org.springframework.stereotype.Component;

@Component
public class BranchMatcher implements PricingConditionMatcher {
    @Override
    public boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer) {
        if (showtime.getRoom() == null || showtime.getRoom().getBranch() == null) {
            return false;
        }
        String branchId = String.valueOf(showtime.getRoom().getBranch().getId());
        return branchId.equals(condition.getValue());
    }
}
