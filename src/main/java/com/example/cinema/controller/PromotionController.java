package com.example.cinema.controller;

import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.repository.PromotionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        LocalDate today = LocalDate.now();
        List<PromotionResponse> promotions = promotionRepository
                .findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
                .stream()
                .map(promotion -> {
                    PromotionResponse response = new PromotionResponse();
                    response.setCode(promotion.getCode());
                    response.setName(promotion.getName());
                    response.setDiscountType(promotion.getDiscountType());
                    response.setDiscountValue(promotion.getDiscountValue());
                    response.setMinTier(promotion.getMinTier());
                    response.setStartDate(promotion.getStartDate());
                    response.setEndDate(promotion.getEndDate());
                    return response;
                })
                .toList();
        return ResponseEntity.ok(promotions);
    }
}
