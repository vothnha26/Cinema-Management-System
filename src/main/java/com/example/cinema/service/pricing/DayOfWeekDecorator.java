package com.example.cinema.service.pricing;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class DayOfWeekDecorator extends PriceDecorator {
    private final LocalDateTime startTime;

    public DayOfWeekDecorator(PriceCalculator calculator, LocalDateTime startTime) {
        super(calculator);
        this.startTime = startTime;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrappedCalculator.calculate();
        DayOfWeek day = startTime.getDayOfWeek();
        
        // Giảm giá 10k cho Thứ 2 và Thứ 3 (Early Week Discount)
        if (day == DayOfWeek.MONDAY || day == DayOfWeek.TUESDAY) {
            return base.subtract(new BigDecimal("10000.00"));
        }
        
        // Phụ phí 10k cho Thứ 7 và Chủ Nhật (Weekend Surcharge)
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return base.add(new BigDecimal("10000.00"));
        }
        
        return base;
    }
}
