package com.example.cinema.model.enums;

public enum PricingImpactType {
    ADDITIVE,       // Cộng thêm số tiền (v.d: +10,000đ)
    PERCENTAGE,     // Nhân tỉ lệ (v.d: x1.2)
    SUBTRACTIVE,    // Trừ bớt (v.d: -10,000đ)
    FIXED           // Ghi đè giá trị cố định (v.d: Mọi vé 45,000đ cho ngày thứ 4 vui vẻ)
}
