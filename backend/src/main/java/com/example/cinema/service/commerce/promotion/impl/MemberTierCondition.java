package com.example.cinema.service.commerce.promotion.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.service.commerce.promotion.IPromotionCondition;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MemberTierCondition implements IPromotionCondition {
    @Override
    public void check(Promotion p, BigDecimal amount, Customer c) {
        if (p.getMinLevel() != null) {
            if (c == null || c.getMembershipLevel() == null || 
                c.getMembershipLevel().getPriority() < p.getMinLevel().getPriority()) {
                throw new AppException("Hạng thành viên của bạn không đủ điều kiện áp dụng mã này");
            }
        }
    }
}
