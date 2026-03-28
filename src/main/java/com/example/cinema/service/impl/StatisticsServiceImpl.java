package com.example.cinema.service.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.repository.BookingRepository;
import com.example.cinema.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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

        // 1. Lấy doanh thu theo ngày (7 ngày gần nhất)
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> dailyRaw = bookingRepository.calculateRevenueByDate(sevenDaysAgo);
        
        Map<String, BigDecimal> revenueByDay = new HashMap<>();
        // Khởi tạo 7 ngày với giá trị 0
        for (int i = 0; i < 7; i++) {
            revenueByDay.put(LocalDate.now().minusDays(i).toString(), BigDecimal.ZERO);
        }
        // Điền dữ liệu thật từ DB
        for (Object[] row : dailyRaw) {
            if (row[0] != null) {
                revenueByDay.put(row[0].toString(), (BigDecimal) row[1]);
            }
        }

        // 2. Giả lập doanh thu theo phim
        Map<String, BigDecimal> movieRevenue = new HashMap<>();
        movieRevenue.put("Avengers: Secret Wars", totalTicketRevenue.multiply(new BigDecimal("0.4")));
        movieRevenue.put("The Dark Knight", totalTicketRevenue.multiply(new BigDecimal("0.35")));
        movieRevenue.put("Jurassic World", totalTicketRevenue.multiply(new BigDecimal("0.25")));

        return new StatisticsResponse.Builder()
                .totalRevenue(totalTicketRevenue)
                .ticketRevenue(totalTicketRevenue)
                .comboRevenue(BigDecimal.ZERO)
                .totalTickets(totalTickets)
                .occupancyRate(68.5)
                .revenueByMovie(movieRevenue)
                .revenueByDay(revenueByDay) // Cần thêm field này vào DTO
                .build();
    }
}
