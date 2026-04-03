package com.example.cinema;

import com.example.cinema.model.dto.request.*;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class Member2ComprehensiveTest extends BaseIntegTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    @DisplayName("Manager-Flow: Kiểm thử tích hợp toàn diện các module của Manager")
    void testManagerComprehensiveWorkflow() throws Exception {
        // 1. Movie
        MovieRequest movieRequest = new MovieRequest();
        movieRequest.setTitle("Doctor Strange: Multiverse");
        movieRequest.setDuration(126);
        movieRequest.setAgeRating("T13");
        movieRequest.setStatus(MovieStatus.COMING);
        movieRequest.setGenreIds(Collections.singletonList(1L));

        String movieJson = objectMapper.writeValueAsString(movieRequest);
        MockMultipartFile moviePart = new MockMultipartFile(
                "movie", "movie.json", MediaType.APPLICATION_JSON_VALUE, movieJson.getBytes());

        MvcResult movieResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/movies")
                        .file(moviePart))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long movieId = objectMapper.readTree(movieResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Room
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setName("IMAX Room 2");
        roomRequest.setType(RoomType.IMAX);
        roomRequest.setRows(10);
        roomRequest.setCols(10);

        MvcResult roomResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long roomId = objectMapper.readTree(roomResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. Showtime
        ShowtimeRequest showtimeRequest = new ShowtimeRequest();
        showtimeRequest.setMovieId(movieId);
        showtimeRequest.setRoomId(roomId);
        showtimeRequest.setStartTime(LocalDateTime.now().plusDays(5));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(showtimeRequest)))
                .andExpect(status().isCreated());

        // 4. Pricing
        SeatPriceRequest priceRequest = new SeatPriceRequest();
        priceRequest.setRoomType(RoomType.IMAX);
        priceRequest.setSeatType(SeatType.VIP);
        priceRequest.setPrice(new BigDecimal("150000"));
        priceRequest.setEffectiveDate(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/pricing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isOk());

        // 5. Promotion
        PromotionRequest promoRequest = new PromotionRequest();
        promoRequest.setCode("MANAGER_TEST_PROMO");
        promoRequest.setName("Member 2 Test Discount");
        promoRequest.setDiscountType(com.example.cinema.model.enums.DiscountType.PERCENT);
        promoRequest.setDiscountValue(BigDecimal.valueOf(20));
        promoRequest.setStartDate(LocalDate.now());
        promoRequest.setEndDate(LocalDate.now().plusWeeks(2));
        promoRequest.setMinOrderAmount(BigDecimal.valueOf(50000));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoRequest)))
                .andExpect(status().isCreated());

        // 6. Audit Log
        mockMvc.perform(MockMvcRequestBuilders.get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
                
        // 7. Statistics
        mockMvc.perform(MockMvcRequestBuilders.get("/api/statistics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
}
