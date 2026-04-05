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
    private final com.example.cinema.repository.showtime.ShowtimeRepository showtimeRepository;

    public StatisticsServiceImpl(BookingRepository bookingRepository, 
                                 CustomerRepository customerRepository,
                                 com.example.cinema.repository.showtime.ShowtimeRepository showtimeRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.showtimeRepository = showtimeRepository;
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
        for (int i = 0; i < 24; i++) revenueByHour.put(i, BigDecimal.ZERO);
        List<Object[]> hourData = bookingRepository.calculateRevenueByHour(start, end);
        for (Object[] row : hourData) {
            revenueByHour.put(((Number) row[0]).intValue(), (BigDecimal) row[1]);
        }

        // Doanh thu theo phòng
        Map<String, BigDecimal> revenueByRoom = new HashMap<>();
        List<Object[]> roomRevData = bookingRepository.calculateRevenueByRoom(start, end);
        for (Object[] row : roomRevData) {
            revenueByRoom.put((String) row[0], (BigDecimal) row[1]);
        }

        // Tỷ lệ lấp đầy theo phòng
        Map<String, Double> occupancyByRoom = new HashMap<>();
        List<com.example.cinema.model.entity.Showtime> showtimes = showtimeRepository.findAllByStartTimeBetween(start, end);
        
        Map<String, long[]> roomStats = new HashMap<>(); // RoomName -> [totalSold, totalCapacity]
        for (com.example.cinema.model.entity.Showtime s : showtimes) {
            String roomName = s.getRoom().getName();
            long[] stats = roomStats.getOrDefault(roomName, new long[]{0, 0});
            stats[0] += s.getSoldSeats();
            stats[1] += s.getTotalSeats();
            roomStats.put(roomName, stats);
        }

        double overallOccupancySum = 0;
        int roomCount = 0;
        for (Map.Entry<String, long[]> entry : roomStats.entrySet()) {
            long sold = entry.getValue()[0];
            long cap = entry.getValue()[1];
            double rate = cap > 0 ? (double) sold / cap * 100 : 0;
            occupancyByRoom.put(entry.getKey(), Math.round(rate * 10.0) / 10.0);
            overallOccupancySum += rate;
            roomCount++;
        }

        double avgOccupancy = roomCount > 0 ? overallOccupancySum / roomCount : 0;

        return new StatisticsResponse.Builder()
                .totalRevenue(totalRevenue)
                .ticketRevenue(totalRevenue)
                .comboRevenue(BigDecimal.ZERO)
                .totalTickets(totalTickets)
                .newCustomers(newCustomers)
                .occupancyRate(Math.round(avgOccupancy * 10.0) / 10.0)
                .revenueByMovie(revenueByMovie)
                .revenueByDay(revenueByDay)
                .revenueByHour(revenueByHour)
                .revenueByRoom(revenueByRoom)
                .occupancyByRoom(occupancyByRoom)
                .build();
    }
}
