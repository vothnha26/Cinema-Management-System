package com.example.cinema.commerce;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.repository.commerce.ComboRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ComboIntegTest extends BaseIntegTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    @DisplayName("MGT-2.2: Test luồng tạo Combo thực tế với ảnh Cloudinary")
    void testRealComboCreationFlow() throws Exception {
        ComboRequest request = new ComboRequest();
        request.setName("Combo Bắp Nước Siêu Cấp " + System.currentTimeMillis());
        request.setDescription("Bao gồm 1 bắp lớn và 2 Coca");
        request.setPrice(BigDecimal.valueOf(150000.0));
        request.setStockQuantity(100);

        String comboJson = objectMapper.writeValueAsString(request);

        byte[] validImageBytes = java.util.Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
        MockMultipartFile imageFile = new MockMultipartFile(
            "image", "combo.gif", "image/gif", validImageBytes
        );

        MockMultipartFile comboPart = new MockMultipartFile(
            "combo", "", "application/json", comboJson.getBytes(StandardCharsets.UTF_8)
        );

        String responseContent = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/combos")
                .file(imageFile)
                .file(comboPart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(responseContent.contains("imageUrl"));
        assertTrue(responseContent.contains("cloudinary"));
    }
}
