package com.example.cinema.service.commerce.promotion.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.service.commerce.promotion.IPromotionCondition;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MinOrderCondition implements IPromotionCondition {
    @Override
    public void check(Promotion p, BigDecimal amount, Customer c) {
        if (p.getMinOrderAmount() != null && amount.compareTo(p.getMinOrderAmount()) < 0) {
            throw new AppException("Đơn hàng chưa đạt giá trị tối thiểu: " + p.getMinOrderAmount() + "đ");
        }
    }
}
