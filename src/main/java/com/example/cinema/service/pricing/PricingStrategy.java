package com.example.cinema.service.pricing;

import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculateSurcharge(Showtime showtime, Seat seat);
}
