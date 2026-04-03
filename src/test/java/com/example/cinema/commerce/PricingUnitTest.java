package com.example.cinema.commerce;

import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import com.example.cinema.service.commerce.pricing.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingUnitTest {

    @Test
    @DisplayName("SLS-4.2: Test logic tính giá áp dụng Decorator Pattern (Base + VIP + Hall3D)")
    void testPriceDecoratorLogic() {
        // Base Price: 60,000
        PriceCalculator calculator = new BasePriceCalculator(new BigDecimal("60000.00"));

        // Add Seat Type VIP: +20,000
        calculator = new SeatTypeDecorator(calculator, SeatType.VIP);

        // Add Room Type 3D: +20,000 (Giả định Hall_3D +20k)
        calculator = new RoomTypeDecorator(calculator, RoomType.HALL_3D);

        // Final Price should be 110,000 (60k base + 20k VIP + 30k Hall_3D)
        BigDecimal finalPrice = calculator.calculate();
        
        // Dùng compareTo vì BigDecimal có thể khác scale (.00 vs .0)
        assertEquals(0, new BigDecimal("110000.00").compareTo(finalPrice), 
            "Giá cuối cùng phải là 110,000 sau khi decorators áp dụng phụ phí");
    }
}
