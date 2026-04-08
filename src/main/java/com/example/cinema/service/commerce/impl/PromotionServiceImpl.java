package com.example.cinema.service.commerce.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.MembershipLevel;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.service.commerce.pricing.DiscountStrategy;
import com.example.cinema.service.commerce.pricing.DiscountStrategyFactory;
import com.example.cinema.service.commerce.PromotionService;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CustomerRepository customerRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final MembershipBenefitRepository benefitRepository;
    private final DiscountStrategyFactory strategyFactory;
    private final ModelMapper modelMapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository,
            CustomerRepository customerRepository,
            MembershipLevelRepository membershipLevelRepository,
            MembershipBenefitRepository benefitRepository,
            DiscountStrategyFactory strategyFactory,
            ModelMapper modelMapper) {
        this.promotionRepository = promotionRepository;
        this.customerRepository = customerRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.benefitRepository = benefitRepository;
        this.strategyFactory = strategyFactory;
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
        LocalDate now = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> p.getIsActive() && 
                            !now.isBefore(p.getStartDate()) && 
                            !now.isAfter(p.getEndDate()) &&
                            (p.getUsageLimit() == null || p.getUsedCount() < p.getUsageLimit()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @com.example.cinema.config.LogAction(action = "CREATE", target = "PROMOTION")
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new AppException("Mã khuyến mãi đã tồn tại");
        }

        MembershipLevel level = membershipLevelRepository.findByName(request.getMinLevelName() != null ? request.getMinLevelName() : "STANDARD")
                .orElseThrow(() -> new AppException("Không tìm thấy hạng thành viên: " + request.getMinLevelName()));

        Promotion promotion = new Promotion.Builder(
                request.getCode(),
                request.getName(),
                request.getDiscountType(),
                request.getDiscountValue())
                .validity(request.getStartDate(), request.getEndDate())
                .minOrder(request.getMinOrderAmount())
                .maxDiscount(request.getMaxDiscountAmount())
                .limit(request.getUsageLimit())
                .minLevel(level)
                .requiredPoints(request.getRequiredPoints())
                .redeemable(request.getIsRedeemable())
                .build();

        Promotion saved = promotionRepository.save(promotion);
        return mapToResponse(saved);
    }

    private PromotionResponse mapToResponse(Promotion p) {
        PromotionResponse res = modelMapper.map(p, PromotionResponse.class);
        if (p.getMinLevel() != null) {
            res.setMinLevelName(p.getMinLevel().getName());
        }
        return res;
    }

    @Override
    public PromotionResponse getPromotionById(Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi"));
        return mapToResponse(p);
    }

    @Override
    public PromotionResponse validatePromotion(String code, BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new AppException("Mã khuyến mãi không tồn tại"));

        validatePromotionRules(promotion, orderAmount);

        DiscountStrategy strategy = strategyFactory.getStrategy(promotion.getDiscountType());
        BigDecimal discountAmount = strategy.calculateDiscount(promotion, orderAmount);

        PromotionResponse response = mapToResponse(promotion);
        response.setAppliedDiscountAmount(discountAmount);

        return response;
    }

    private void validatePromotionRules(Promotion promotion, BigDecimal orderAmount) {
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

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Customer customer = customerRepository.findByUserUsername(username).orElse(null);

            if (customer != null && promotion.getMinLevel() != null) {
                if (customer.getMembershipLevel() == null || 
                    customer.getMembershipLevel().getPriority() < promotion.getMinLevel().getPriority()) {
                    throw new AppException("Hạng thành viên của bạn (" + (customer.getMembershipLevel() != null ? customer.getMembershipLevel().getName() : "GUEST") +
                            ") chưa đủ điều kiện áp dụng mã này (Yêu cầu tối thiểu: " + promotion.getMinLevel().getName() + ")");
                }
            }
        }
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
