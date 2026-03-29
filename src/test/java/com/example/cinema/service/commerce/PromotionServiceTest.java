package com.example.cinema.service.commerce;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.response.PromotionResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.commerce.impl.PromotionServiceImpl;
import com.example.cinema.service.commerce.pricing.DiscountStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Spy
    private DiscountStrategyFactory strategyFactory = new DiscountStrategyFactory();
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion percentPromo;
    private Customer goldCustomer;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        
        percentPromo = new Promotion.Builder("SAVE10", "Giảm 10%", DiscountType.PERCENT, new BigDecimal("10"))
                .validity(LocalDate.now().minusDays(1), LocalDate.now().plusDays(10))
                .minOrder(new BigDecimal("100000"))
                .maxDiscount(new BigDecimal("50000"))
                .minTier(MembershipTier.GOLD)
                .build();
        percentPromo.setUsedCount(0);
        percentPromo.setUsageLimit(100);

        goldCustomer = new Customer();
        goldCustomer.setMembershipTier(MembershipTier.GOLD);
    }

    @Test
    @DisplayName("Validate thành công và tính đúng số tiền giảm %")
    void validatePromotion_Success_Percentage() {
        // Arrange
        String code = "SAVE10";
        BigDecimal orderAmount = new BigDecimal("200000"); // 10% của 200k là 20k
        
        when(promotionRepository.findByCode(code)).thenReturn(Optional.of(percentPromo));
        setupSecurityContext("member_gold");
        when(customerRepository.findByUserUsername("member_gold")).thenReturn(Optional.of(goldCustomer));

        // Act
        PromotionResponse response = promotionService.validatePromotion(code, orderAmount);

        // Assert
        assertNotNull(response);
        assertEquals(0, new BigDecimal("20000.00").compareTo(response.getAppliedDiscountAmount()));
        verify(promotionRepository).findByCode(code);
    }

    @Test
    @DisplayName("Thất bại khi khách hàng không đủ hạng thành viên (Tier Check)")
    void validatePromotion_Fail_InsufficientTier() {
        // Arrange
        Customer silverCustomer = new Customer();
        silverCustomer.setMembershipTier(MembershipTier.SILVER); // SILVER < GOLD
        
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(percentPromo));
        setupSecurityContext("member_silver");
        when(customerRepository.findByUserUsername("member_silver")).thenReturn(Optional.of(silverCustomer));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> 
                promotionService.validatePromotion("SAVE10", new BigDecimal("200000")));
        
        assertTrue(exception.getMessage().contains("chưa đủ điều kiện"));
    }

    @Test
    @DisplayName("Thất bại khi mã đã hết lượt sử dụng")
    void validatePromotion_Fail_UsageLimit() {
        // Arrange
        percentPromo.setUsedCount(100);
        percentPromo.setUsageLimit(100);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(percentPromo));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> 
                promotionService.validatePromotion("SAVE10", new BigDecimal("200000")));
        
        assertEquals("Mã khuyến mãi đã hết lượt sử dụng", exception.getMessage());
    }

    private void setupSecurityContext(String username) {
        User principal = new User(username, "password", Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
    }
}
