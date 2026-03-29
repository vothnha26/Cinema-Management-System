package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.entity.Promotion;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        BigDecimal discount = orderAmount.multiply(promotion.getDiscountValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Kiểm tra mức giảm tối đa
        if (promotion.getMaxDiscountAmount() != null && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            return promotion.getMaxDiscountAmount();
        }
        return discount;
    }
}
