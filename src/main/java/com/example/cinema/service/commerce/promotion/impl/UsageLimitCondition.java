package com.example.cinema.service.commerce.promotion.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.service.commerce.promotion.IPromotionCondition;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class UsageLimitCondition implements IPromotionCondition {
    @Override
    public void check(Promotion p, BigDecimal amount, Customer c) {
        if (p.getUsageLimit() != null && p.getUsedCount() >= p.getUsageLimit()) {
            throw new AppException("Mã khuyến mãi đã hết lượt sử dụng");
        }
    }
}
