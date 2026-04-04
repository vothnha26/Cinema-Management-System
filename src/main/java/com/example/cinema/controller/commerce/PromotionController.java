package com.example.cinema.controller.commerce;

import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.service.commerce.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getAllPromotions()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getActivePromotions()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@RequestBody @Valid PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(promotionService.createPromotion(request)));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<PromotionResponse>> validatePromotion(
            @RequestParam String code, 
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.validatePromotion(code, amount)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
