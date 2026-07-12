package com.example.cinema.service.commerce.promotion;

import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.entity.Customer;
import java.math.BigDecimal;

public interface IPromotionCondition {
    void check(Promotion promotion, BigDecimal orderAmount, Customer customer);
}
