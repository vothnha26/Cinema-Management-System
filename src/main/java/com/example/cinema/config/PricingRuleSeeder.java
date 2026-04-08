package com.example.cinema.config;

import com.example.cinema.model.entity.PricingCondition;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.model.enums.PricingImpactType;
import com.example.cinema.model.enums.PricingRuleCategory;
import com.example.cinema.service.commerce.PricingRuleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

//@org.springframework.context.annotation.Configuration
@org.springframework.core.annotation.Order(5)
public class PricingRuleSeeder {

    @Bean
    @Order(10)
    public CommandLineRunner initPricingRules(PricingRuleService pricingRuleService, com.example.cinema.repository.branch.BranchRepository branchRepository) {
        return args -> {
            if (!pricingRuleService.getAllRules().isEmpty()) return;

            // Lấy chi nhánh Quận 1 để làm local rule
            com.example.cinema.model.entity.Branch branchQ1 = branchRepository.findAll().stream()
                .filter(b -> b.getName().contains("Quận 1"))
                .findFirst().orElse(null);

            // 1. Thứ 4 Vui Vẻ - Giá cố định 45k (GLOBAL)
            PricingRule happyWednesday = new PricingRule();
            happyWednesday.setName("Thứ 4 Vui Vẻ");
            happyWednesday.setDescription("Đồng giá 45,000đ cho mọi suất chiếu vào Thứ 4");
            happyWednesday.setCategory(PricingRuleCategory.BASE);
            happyWednesday.setImpactType(PricingImpactType.FIXED);
            happyWednesday.setImpactValue(new BigDecimal("45000"));
            happyWednesday.setStackable(false);
            happyWednesday.addCondition(new PricingCondition(happyWednesday, PricingConditionType.DAY_OF_WEEK, "WEDNESDAY", "Chỉ áp dụng Thứ 4"));
            pricingRuleService.createRule(happyWednesday);

            // 2. Suất chiếu sớm (Morning) - Giảm 20% (GLOBAL)
            PricingRule earlyBird = new PricingRule();
            earlyBird.setName("Suất chiếu sớm");
            earlyBird.setDescription("Giảm 20% cho các suất chiếu trước 10:00 sáng");
            earlyBird.setCategory(PricingRuleCategory.DISCOUNT);
            earlyBird.setImpactType(PricingImpactType.PERCENTAGE);
            earlyBird.setImpactValue(new BigDecimal("0.8"));
            earlyBird.setStackable(true);
            earlyBird.addCondition(new PricingCondition(earlyBird, PricingConditionType.TIME_RANGE, "00:00-10:00", "Trước 10 giờ sáng"));
            pricingRuleService.createRule(earlyBird);

            // 3. Phụ thu Cuối tuần - +15,000đ (GLOBAL)
            PricingRule weekendSurcharge = new PricingRule();
            weekendSurcharge.setName("Phụ thu Cuối tuần");
            weekendSurcharge.setDescription("Phụ thu 15,000đ cho Thứ 7 và Chủ Nhật");
            weekendSurcharge.setCategory(PricingRuleCategory.SURCHARGE);
            weekendSurcharge.setImpactType(PricingImpactType.ADDITIVE);
            weekendSurcharge.setImpactValue(new BigDecimal("15000"));
            weekendSurcharge.setStackable(true);
            weekendSurcharge.addCondition(new PricingCondition(weekendSurcharge, PricingConditionType.DAY_OF_WEEK, "SATURDAY,SUNDAY", "Thứ 7 & Chủ Nhật"));
            pricingRuleService.createRule(weekendSurcharge);

            // 4. Ưu đãi chi nhánh Quận 1 - Giảm 5,000đ (LOCAL)
            if (branchQ1 != null) {
                PricingRule q1Special = new PricingRule();
                q1Special.setName("Ưu đãi Quận 1");
                q1Special.setDescription("Giảm 5,000đ cho khách hàng tại chi nhánh Quận 1");
                q1Special.setCategory(PricingRuleCategory.DISCOUNT);
                q1Special.setImpactType(PricingImpactType.ADDITIVE);
                q1Special.setImpactValue(new BigDecimal("-5000"));
                q1Special.setStackable(true);
                q1Special.addCondition(new PricingCondition(q1Special, PricingConditionType.BRANCH, String.valueOf(branchQ1.getId()), "Chi nhánh Quận 1"));
                pricingRuleService.createRule(q1Special);
            }

            // 5. Ưu đãi hạng GOLD - Giảm 10% (GLOBAL)
            PricingRule goldDiscount = new PricingRule();
            goldDiscount.setName("Ưu đãi thành viên GOLD");
            goldDiscount.setDescription("Giảm 10% cho khách hàng hạng GOLD");
            goldDiscount.setCategory(PricingRuleCategory.DISCOUNT);
            goldDiscount.setImpactType(PricingImpactType.PERCENTAGE);
            goldDiscount.setImpactValue(new BigDecimal("0.9"));
            goldDiscount.setStackable(true);
            goldDiscount.addCondition(new PricingCondition(goldDiscount, PricingConditionType.MEMBER_TIER, "GOLD", "Dành cho hạng GOLD"));
            pricingRuleService.createRule(goldDiscount);

            System.out.println("✅ Đã khởi tạo Quy tắc giá mẫu (Global & Local)");
        };
    }
}
