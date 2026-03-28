package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.enums.RoomType;
import java.math.BigDecimal;

public class RoomTypeDecorator extends PriceDecorator {
    private final RoomType roomType;

    public RoomTypeDecorator(PriceCalculator calculator, RoomType roomType) {
        super(calculator);
        this.roomType = roomType;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal surcharge = BigDecimal.ZERO;
        switch (roomType) {
            case IMAX: surcharge = new BigDecimal("50000.00"); break;
            case FOUR_DX: surcharge = new BigDecimal("80000.00"); break;
            case LUXURY: surcharge = new BigDecimal("100000.00"); break;
            default: break;
        }
        return wrappedCalculator.calculate().add(surcharge);
    }
}
