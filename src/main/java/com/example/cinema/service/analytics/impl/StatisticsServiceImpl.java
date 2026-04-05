package com.example.cinema.service.analytics.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.analytics.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public StatisticsServiceImpl(BookingRepository bookingRepository, CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalTickets = bookingRepository.countTotalTickets();
        long newCustomers = customerRepository.countNewCustomers(start, end);

        // Doanh thu theo ngày
        Map<String, BigDecimal> revenueByDay = new TreeMap<>();
        List<Object[]> dayData = bookingRepository.calculateRevenueByDay(start, end);
        for (Object[] row : dayData) {
            revenueByDay.put(row[0].toString(), (BigDecimal) row[1]);
        }

        // Doanh thu theo phim
        Map<String, BigDecimal> revenueByMovie = new HashMap<>();
        List<Object[]> movieData = bookingRepository.calculateRevenueByMovie(start, end);
        for (Object[] row : movieData) {
            revenueByMovie.put((String) row[0], (BigDecimal) row[1]);
        }

        // Doanh thu theo giờ
        Map<Integer, BigDecimal> revenueByHour = new TreeMap<>();
        // Khởi tạo 24 giờ với giá trị 0
        for (int i = 0; i < 24; i++) revenueByHour.put(i, BigDecimal.ZERO);
        
        List<Object[]> hourData = bookingRepository.calculateRevenueByHour(start, end);
        for (Object[] row : hourData) {
            revenueByHour.put(((Number) row[0]).intValue(), (BigDecimal) row[1]);
        }

        return new StatisticsResponse.Builder()
                .totalRevenue(totalRevenue)
                .ticketRevenue(totalRevenue) // Giả định ticketRevenue = totalRevenue vì chưa tách combo
                .comboRevenue(BigDecimal.ZERO)
                .totalTickets(totalTickets)
                .newCustomers(newCustomers)
                .occupancyRate(72.4) // Mock rate, cần query thêm từ Showtime nếu muốn chính xác
                .revenueByMovie(revenueByMovie)
                .revenueByDay(revenueByDay)
                .revenueByHour(revenueByHour)
                .build();
    }
}
