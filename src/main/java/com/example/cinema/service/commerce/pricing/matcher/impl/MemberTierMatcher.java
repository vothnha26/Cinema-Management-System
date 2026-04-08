package com.example.cinema.service.commerce.pricing.matcher.impl;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.commerce.pricing.matcher.PricingConditionMatcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MemberTierMatcher implements PricingConditionMatcher {

    private final CustomerRepository customerRepository;

    public MemberTierMatcher(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public boolean matches(PricingCondition condition, Showtime showtime, Seat seat, Customer customer) {
        String currentTier = "GUEST"; // Default for walk-in/unauthenticated

        if (customer != null) {
            currentTier = customer.getMembershipLevel() != null ? 
                          customer.getMembershipLevel().getName() : "GUEST";
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                String username = auth.getName();
                Optional<Customer> customerOpt = customerRepository.findByUserUsername(username);
                if (customerOpt.isPresent()) {
                    currentTier = customerOpt.get().getMembershipLevel() != null ? 
                                  customerOpt.get().getMembershipLevel().getName() : "GUEST";
                }
            }
        }

        return currentTier.equalsIgnoreCase(condition.getValue());
    }
}
