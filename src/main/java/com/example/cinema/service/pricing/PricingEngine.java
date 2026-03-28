package com.example.cinema.service.pricing;

import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class PricingEngine {
    private final List<PricingStrategy> strategies;

    public PricingEngine(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal calculateTotal(BigDecimal basePrice, Showtime showtime, Seat seat) {
        BigDecimal total = basePrice;
        for (PricingStrategy strategy : strategies) {
            total = total.add(strategy.calculateSurcharge(showtime, seat));
        }
        return total;
    }
}
