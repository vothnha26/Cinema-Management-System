package com.example.cinema.model.enums;

public enum PricingRuleType {
    ADDITIVE,    // Cộng thêm (v.d: +10,000đ)
    SUBTRACTIVE, // Trừ bớt (v.d: -10,000đ)
    PERCENTAGE,  // Nhân tỉ lệ (v.d: x1.2 cho 20% tăng thêm)
    OVERRIDE     // Ghi đè (Dùng cho trường hợp đặc biệt)
}
