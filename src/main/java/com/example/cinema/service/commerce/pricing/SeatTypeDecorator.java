package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.enums.SeatType;
import java.math.BigDecimal;

public class SeatTypeDecorator extends PriceDecorator {
    private final SeatType seatType;

    public SeatTypeDecorator(PriceCalculator calculator, SeatType seatType) {
        super(calculator);
        this.seatType = seatType;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal surcharge = BigDecimal.ZERO;
        if (seatType == SeatType.VIP) {
            surcharge = new BigDecimal("20000.00");
        } else if (seatType == SeatType.COUPLE) {
            surcharge = new BigDecimal("40000.00");
        }
        return wrappedCalculator.calculate().add(surcharge);
    }
}
