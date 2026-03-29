package com.example.cinema.service.commerce.pricing;

import com.example.cinema.model.entity.Promotion;
import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount);
}
