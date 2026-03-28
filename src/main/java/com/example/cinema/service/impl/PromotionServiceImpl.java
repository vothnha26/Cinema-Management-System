package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.repository.PromotionRepository;
import com.example.cinema.service.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ModelMapper modelMapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository, ModelMapper modelMapper) {
        this.promotionRepository = promotionRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(p -> modelMapper.map(p, PromotionResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "CREATE", target = "PROMOTION")
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new AppException("Mã khuyến mãi đã tồn tại");
        }

        // Áp dụng Builder Pattern để tạo đối tượng Promotion
        Promotion promotion = new Promotion.Builder(
                request.getCode(),
                request.getName(),
                request.getDiscountType(),
                request.getDiscountValue()
        )
        .validity(request.getStartDate(), request.getEndDate())
        .minOrder(request.getMinOrderAmount())
        .maxDiscount(request.getMaxDiscountAmount())
        .limit(request.getUsageLimit())
        .minTier(request.getMinTier())
        .build();

        Promotion saved = promotionRepository.save(promotion);
        return modelMapper.map(saved, PromotionResponse.class);
    }

    @Override
    public PromotionResponse validatePromotion(String code, BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new AppException("Mã khuyến mãi không tồn tại"));

        if (!promotion.getIsActive()) {
            throw new AppException("Mã khuyến mãi đã bị vô hiệu hóa");
        }

        LocalDate now = LocalDate.now();
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new AppException("Mã khuyến mãi đã hết hạn hoặc chưa đến thời gian áp dụng");
        }

        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new AppException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (promotion.getMinOrderAmount() != null && orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new AppException("Đơn hàng chưa đạt giá trị tối thiểu " + promotion.getMinOrderAmount() + " VNĐ");
        }

        return modelMapper.map(promotion, PromotionResponse.class);
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "DELETE", target = "PROMOTION")
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new AppException("Không tìm thấy chương trình khuyến mãi");
        }
        promotionRepository.deleteById(id);
    }
}
