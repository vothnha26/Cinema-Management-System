package com.example.cinema.service.commerce;

import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.PromotionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAllPromotions();
    List<PromotionResponse> getActivePromotions();
    PromotionResponse getPromotionById(Long id);
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(Long id, PromotionRequest request);
    PromotionResponse validatePromotion(String code, BigDecimal orderAmount, String phone);

    void deletePromotion(Long id);
}
