package com.example.cinema.service.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeSlotDecorator extends PriceDecorator {
    private final LocalDateTime startTime;

    public TimeSlotDecorator(PriceCalculator calculator, LocalDateTime startTime) {
        super(calculator);
        this.startTime = startTime;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrappedCalculator.calculate();
        LocalTime time = startTime.toLocalTime();
        
        // Happy Hour: Suất chiếu sáng sớm (trước 12h) giảm 15k
        if (time.isBefore(LocalTime.of(12, 0))) {
            return base.subtract(new BigDecimal("15000.00"));
        }
        
        // Suất chiếu muộn (sau 22h) giảm 10k
        if (time.isAfter(LocalTime.of(22, 0))) {
            return base.subtract(new BigDecimal("10000.00"));
        }
        
        return base;
    }
}
