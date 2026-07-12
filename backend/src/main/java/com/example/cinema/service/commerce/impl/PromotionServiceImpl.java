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
import com.example.cinema.service.commerce.promotion.IPromotionCondition;
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
    private final List<IPromotionCondition> conditions;

    public PromotionServiceImpl(PromotionRepository promotionRepository,
                                MembershipLevelRepository membershipLevelRepository,
                                CustomerRepository customerRepository,
                                ModelMapper modelMapper,
                                List<IPromotionCondition> conditions) {
        this.promotionRepository = promotionRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
        this.conditions = conditions;
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findAll().stream()
                .filter(p -> p.getIsActive() && p.getEndDate().isAfter(java.time.LocalDate.now()))
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PromotionResponse getPromotionById(Long id) {
        return promotionRepository.findById(id).map(this::mapToResponse).orElseThrow(() -> new AppException("Không tìm thấy khuyến mãi"));
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) throw new AppException("Mã đã tồn tại");
        Promotion p = modelMapper.map(request, Promotion.class);
        if (request.getMinLevelName() != null) {
            p.setMinLevel(membershipLevelRepository.findByName(request.getMinLevelName()).orElse(null));
        }
        return mapToResponse(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion p = promotionRepository.findById(id).orElseThrow();
        modelMapper.map(request, p);
        return mapToResponse(promotionRepository.save(p));
    }

    @Override
    public PromotionResponse validatePromotion(String code, BigDecimal amount, String phone) {
        Promotion p = promotionRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new AppException("Mã khuyến mãi không hợp lệ"));

        Customer customer = findCustomer(phone);
        
        // Chạy chuỗi kiểm tra điều kiện (Triệt tiêu if-else)
        conditions.forEach(cond -> cond.check(p, amount, customer));

        PromotionResponse res = mapToResponse(p);
        BigDecimal discount = calculateDiscount(p, amount);
        res.setAppliedDiscountAmount(discount);
        return res;
    }

    private Customer findCustomer(String phone) {
        if (phone != null && !phone.isEmpty()) return customerRepository.findByPhone(phone).orElse(null);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) return customerRepository.findByUserUsername(((UserDetails) principal).getUsername()).orElse(null);
        return null;
    }

    private BigDecimal calculateDiscount(Promotion p, BigDecimal amount) {
        if (p.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal disc = amount.multiply(p.getDiscountValue()).divide(new BigDecimal("100"));
            if (p.getMaxDiscountAmount() != null && disc.compareTo(p.getMaxDiscountAmount()) > 0) return p.getMaxDiscountAmount();
            return disc;
        }
        return p.getDiscountValue();
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) { promotionRepository.deleteById(id); }

    private PromotionResponse mapToResponse(Promotion p) {
        PromotionResponse res = modelMapper.map(p, PromotionResponse.class);
        if (p.getMinLevel() != null) res.setMinLevelName(p.getMinLevel().getName());
        return res;
    }
}
