package com.example.cinema.service.pricing;

import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.SeatType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SeatTypeStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculateSurcharge(Showtime showtime, Seat seat) {
        if (seat.getType() == SeatType.VIP) {
            return new BigDecimal("20000.00"); // Phụ phí VIP 20k
        } else if (seat.getType() == SeatType.COUPLE) {
            return new BigDecimal("40000.00"); // Phụ phí Couple 40k
        }
        return BigDecimal.ZERO;
    }
}
