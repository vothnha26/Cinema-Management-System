package com.example.cinema.controller;

import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.enums.PaymentMethod;
import com.example.cinema.service.booking.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private com.example.cinema.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.example.cinema.repository.user.UserRepository userRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequest();
        bookingRequest.setShowtimeId(1L);
        bookingRequest.setSeatIds(List.of(1L, 2L));
        bookingRequest.setPaymentMethod(PaymentMethod.CASH);

        bookingResponse = new BookingResponse();
        bookingResponse.setId(100L);
        bookingResponse.setBookingCode("BOOK12345");
        bookingResponse.setTotalPrice(BigDecimal.valueOf(180000));
        bookingResponse.setStatus(com.example.cinema.model.enums.BookingStatus.CONFIRMED);
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testCreateBooking_Success() throws Exception {
        when(bookingService.createPOSBooking(any(BookingRequest.class))).thenReturn(bookingResponse);

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.bookingCode").value("BOOK12345"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testHoldSeat_Success() throws Exception {
        doNothing().when(bookingService).holdSeat(1L, 10L, "session-xyz");

        mockMvc.perform(post("/api/bookings/hold-seat")
                        .with(csrf())
                        .param("showtimeId", "1")
                        .param("seatId", "10")
                        .param("sessionId", "session-xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testReleaseSeat_Success() throws Exception {
        doNothing().when(bookingService).releaseSeat(1L, 10L, "session-xyz");

        mockMvc.perform(post("/api/bookings/release-seat")
                        .with(csrf())
                        .param("showtimeId", "1")
                        .param("seatId", "10")
                        .param("sessionId", "session-xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testGetMyLockedSeats_Success() throws Exception {
        when(bookingService.getMyLockedSeats(1L, "session-xyz")).thenReturn(List.of(10L, 11L));

        mockMvc.perform(get("/api/bookings/my-locked-seats")
                        .param("showtimeId", "1")
                        .param("sessionId", "session-xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value(10L))
                .andExpect(jsonPath("$.data[1]").value(11L));
    }
}
