package com.example.cinema.service.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.repository.BookingRepository;
import com.example.cinema.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final BookingRepository bookingRepository;

    public StatisticsServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalTicketRevenue = bookingRepository.calculateTotalRevenue();
        if (totalTicketRevenue == null) totalTicketRevenue = BigDecimal.ZERO;

        Long totalTickets = bookingRepository.countTotalTickets();

        // Giả lập một số dữ liệu khác cho Dashboard (Task 7)
        Map<String, BigDecimal> movieRevenue = new HashMap<>();
        movieRevenue.put("Avengers: Secret Wars", totalTicketRevenue.multiply(new BigDecimal("0.4")));
        movieRevenue.put("The Dark Knight", totalTicketRevenue.multiply(new BigDecimal("0.35")));
        movieRevenue.put("Jurassic World", totalTicketRevenue.multiply(new BigDecimal("0.25")));

        return new StatisticsResponse.Builder()
                .totalRevenue(totalTicketRevenue)
                .ticketRevenue(totalTicketRevenue)
                .comboRevenue(BigDecimal.ZERO) // Sẽ tính thêm sau khi hoàn thiện BookingCombo
                .totalTickets(totalTickets)
                .occupancyRate(68.5) // Giả lập tỷ lệ lấp đầy
                .revenueByMovie(movieRevenue)
                .build();
    }
}
