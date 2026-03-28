package com.example.cinema.service.commerce;

import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.PromotionResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAllPromotions();
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse validatePromotion(String code, BigDecimal orderAmount);
    void deletePromotion(Long id);
}
