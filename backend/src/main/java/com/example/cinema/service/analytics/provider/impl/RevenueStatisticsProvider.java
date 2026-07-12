package com.example.cinema.service.analytics.provider.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.analytics.provider.IStatisticsProvider;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class RevenueStatisticsProvider implements IStatisticsProvider<StatisticsResponse> {
    private final BookingRepository bookingRepo;
    private final CustomerRepository customerRepo;
    private final ShowtimeRepository showtimeRepo;

    public RevenueStatisticsProvider(BookingRepository b, CustomerRepository c, ShowtimeRepository s) {
        this.bookingRepo = b; this.customerRepo = c; this.showtimeRepo = s;
    }

    @Override public String getProviderType() { return "OVERVIEW"; }

    @Override
    public StatisticsResponse getStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        BigDecimal totalRevenue = Optional.ofNullable(bookingRepo.calculateTotalRevenue(start, end, branchId)).orElse(BigDecimal.ZERO);
        BigDecimal comboRevenue = Optional.ofNullable(bookingRepo.calculateComboRevenue(start, end, branchId)).orElse(BigDecimal.ZERO);
        
        return new StatisticsResponse.Builder()
                .totalRevenue(totalRevenue)
                .comboRevenue(comboRevenue)
                .ticketRevenue(totalRevenue.subtract(comboRevenue))
                .totalTickets(bookingRepo.countTotalTickets(start, end, branchId))
                .newCustomers(customerRepo.countNewCustomers(start, end, branchId))
                .build();
    }
}
