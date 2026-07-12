package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.entity.Promotion;

import java.math.BigDecimal;

public class FixedDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        // Nếu số tiền giảm lớn hơn giá trị đơn hàng, chỉ giảm bằng giá trị đơn hàng
        if (promotion.getDiscountValue().compareTo(orderAmount) > 0) {
            return orderAmount;
        }
        return promotion.getDiscountValue();
    }
}
