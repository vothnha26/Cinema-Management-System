package com.example.cinema.service.pricing;

import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.RoomType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class RoomTypeStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculateSurcharge(Showtime showtime, Seat seat) {
        RoomType type = showtime.getRoom().getType();
        if (type == RoomType.IMAX) {
            return new BigDecimal("50000.00"); // Phụ phí IMAX 50k
        } else if (type == RoomType.FOUR_DX) {
            return new BigDecimal("80000.00"); // Phụ phí 4DX 80k
        } else if (type == RoomType.LUXURY) {
            return new BigDecimal("100000.00"); // Phụ phí Luxury 100k
        }
        return BigDecimal.ZERO;
    }
}
