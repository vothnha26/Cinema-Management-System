package com.example.cinema.commerce;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.dto.request.PromotionRequest;
import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.MembershipTier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class PromotionIntegTest extends BaseIntegTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    @DisplayName("MGT-2.3: Test luồng CRUD Khuyến mãi và Validate mã thực tế")
    void testRealPromotionFlow() throws Exception {
        String promoCode = "PROMO_" + System.currentTimeMillis();

        PromotionRequest request = new PromotionRequest();
        request.setCode(promoCode);
        request.setName("Khai trương");
        request.setDiscountType(DiscountType.PERCENT);
        request.setDiscountValue(BigDecimal.valueOf(20.0));
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setMinOrderAmount(BigDecimal.valueOf(100000));
        request.setMaxDiscountAmount(BigDecimal.valueOf(50000));
        request.setUsageLimit(100);
        request.setMinTier(MembershipTier.SILVER);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/promotions/validate")
                .param("code", promoCode)
                .param("amount", "200000"))
                .andExpect(status().isOk());
    }
}
