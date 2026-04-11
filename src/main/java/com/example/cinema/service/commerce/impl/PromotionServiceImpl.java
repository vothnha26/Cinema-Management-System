package com.example.cinema.service.commerce.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.MembershipLevel;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.service.commerce.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository,
                                MembershipLevelRepository membershipLevelRepository,
                                CustomerRepository customerRepository,
                                ModelMapper modelMapper) {
        this.promotionRepository = promotionRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getActivePromotions() {
        // Lọc thủ công do Repository chưa có sẵn method filter
        return promotionRepository.findAll().stream()
                .filter(p -> p.getIsActive() && p.getEndDate().isAfter(java.time.LocalDate.now()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionResponse getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi"));
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "CREATE", target = "PROMOTION")
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new AppException("Mã khuyến mãi đã tồn tại");
        }

        MembershipLevel level = null;
        if (request.getMinLevelName() != null && !request.getMinLevelName().trim().isEmpty()) {
            level = membershipLevelRepository.findByName(request.getMinLevelName().trim())
                .orElseThrow(() -> new AppException("Không tìm thấy hạng thành viên: " + request.getMinLevelName()));
        }

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
        .minLevel(level)
        .requiredPoints(request.getRequiredPoints())
        .redeemable(request.getIsRedeemable())
        .build();

        return mapToResponse(promotionRepository.save(promotion));
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "UPDATE", target = "PROMOTION")
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi"));

        if (!promotion.getCode().equals(request.getCode()) && promotionRepository.existsByCode(request.getCode())) {
            throw new AppException("Mã khuyến mãi mới đã tồn tại");
        }

        MembershipLevel level = null;
        if (request.getMinLevelName() != null && !request.getMinLevelName().trim().isEmpty()) {
            level = membershipLevelRepository.findByName(request.getMinLevelName().trim())
                .orElseThrow(() -> new AppException("Không tìm thấy hạng thành viên: " + request.getMinLevelName()));
        }

        promotion.setCode(request.getCode());
        promotion.setName(request.getName());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setMinOrderAmount(request.getMinOrderAmount());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setMinLevel(level);
        promotion.setRequiredPoints(request.getRequiredPoints());
        promotion.setIsRedeemable(request.getIsRedeemable());
        promotion.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return mapToResponse(promotionRepository.save(promotion));
    }

    @Override
    @com.example.cinema.config.LogAction(action = "VALIDATE", target = "PROMOTION")
    public PromotionResponse validatePromotion(String code, BigDecimal amount, String phone) {
        Promotion p = promotionRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new AppException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));

        if (p.getEndDate().isBefore(java.time.LocalDate.now())) {
            throw new AppException("Mã khuyến mãi đã hết hạn");
        }

        if (p.getUsageLimit() != null && p.getUsedCount() >= p.getUsageLimit()) {
            throw new AppException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (p.getMinOrderAmount() != null && amount.compareTo(p.getMinOrderAmount()) < 0) {
            throw new AppException("Đơn hàng chưa đạt giá trị tối thiểu: " + p.getMinOrderAmount() + "đ");
        }

        if (p.getMinLevel() != null) {
            Customer customer = null;
            if (phone != null && !phone.isEmpty()) {
                customer = customerRepository.findByPhone(phone).orElse(null);
            }
            if (customer == null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof UserDetails) {
                    customer = customerRepository.findByUserUsername(((UserDetails) principal).getUsername()).orElse(null);
                }
            }

            if (customer == null || customer.getMembershipLevel() == null || 
                customer.getMembershipLevel().getPriority() < p.getMinLevel().getPriority()) {
                throw new AppException("Hạng thành viên của bạn không đủ điều kiện áp dụng mã này");
            }
        }

        PromotionResponse res = mapToResponse(p);
        BigDecimal discount;
        if (p.getDiscountType() == DiscountType.PERCENT) {
            discount = amount.multiply(p.getDiscountValue()).divide(new BigDecimal("100"));
            if (p.getMaxDiscountAmount() != null && p.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(p.getMaxDiscountAmount()) > 0) {
                discount = p.getMaxDiscountAmount();
            }
        } else {
            discount = p.getDiscountValue();
        }
        res.setAppliedDiscountAmount(discount);
        return res;
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

    private PromotionResponse mapToResponse(Promotion p) {
        PromotionResponse res = modelMapper.map(p, PromotionResponse.class);
        if (p.getMinLevel() != null) {
            res.setMinLevelName(p.getMinLevel().getName());
            res.setMinLevelPriority(p.getMinLevel().getPriority());
        } else {
            res.setMinLevelPriority(0);
        }
        return res;
    }
}
