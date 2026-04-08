package com.example.cinema.service.analytics.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import com.example.cinema.model.entity.Booking;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.analytics.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final com.example.cinema.repository.showtime.ShowtimeRepository showtimeRepository;
    private final com.example.cinema.repository.commerce.ComboRepository comboRepository;
    private final com.example.cinema.repository.commerce.PromotionRepository promotionRepository;
    private final com.example.cinema.repository.user.UserRepository userRepository;

    public StatisticsServiceImpl(BookingRepository bookingRepository, 
                                 CustomerRepository customerRepository,
                                 com.example.cinema.repository.showtime.ShowtimeRepository showtimeRepository,
                                 com.example.cinema.repository.commerce.ComboRepository comboRepository,
                                 com.example.cinema.repository.commerce.PromotionRepository promotionRepository,
                                 com.example.cinema.repository.user.UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.showtimeRepository = showtimeRepository;
        this.comboRepository = comboRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue(start, end, branchId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalTickets = bookingRepository.countTotalTickets(start, end, branchId);
        long newCustomers = customerRepository.countNewCustomers(start, end, branchId);

        // Doanh thu theo ngày
        Map<String, BigDecimal> revenueByDay = new TreeMap<>();
        List<Object[]> dayData = bookingRepository.calculateRevenueByDay(start, end, branchId);
        for (Object[] row : dayData) {
            String date = row[0] != null ? row[0].toString() : "N/A";
            BigDecimal revenue = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            revenueByDay.put(date, revenue);
        }

        // Doanh thu theo phim
        Map<String, BigDecimal> revenueByMovie = new HashMap<>();
        List<Object[]> movieData = bookingRepository.calculateRevenueByMovie(start, end, branchId);
        for (Object[] row : movieData) {
            String movie = row[0] != null ? row[0].toString() : "Unknown Movie";
            BigDecimal revenue = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            revenueByMovie.put(movie, revenue);
        }

        // Doanh thu theo giờ
        Map<Integer, BigDecimal> revenueByHour = new TreeMap<>();
        for (int i = 0; i < 24; i++) revenueByHour.put(i, BigDecimal.ZERO);
        List<Object[]> hourData = bookingRepository.calculateRevenueByHour(start, end, branchId);
        if (hourData != null) {
            for (Object[] row : hourData) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    try {
                        int hour = ((Number) row[0]).intValue();
                        BigDecimal revenue = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                        revenueByHour.put(hour, revenue);
                    } catch (Exception e) {
                        System.err.println("Error parsing hour data: " + e.getMessage());
                    }
                }
            }
        }

        // Doanh thu theo phòng
        Map<String, BigDecimal> revenueByRoom = new HashMap<>();
        List<Object[]> roomRevData = bookingRepository.calculateRevenueByRoom(start, end, branchId);
        if (roomRevData != null) {
            for (Object[] row : roomRevData) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    String room = row[0].toString();
                    BigDecimal revenue = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                    revenueByRoom.put(room, revenue);
                }
            }
        }

        // Tỷ lệ lấp đầy theo phòng
        Map<String, Double> occupancyByRoom = new HashMap<>();
        List<com.example.cinema.model.entity.Showtime> showtimes = showtimeRepository.findAllByStartTimeBetween(start, end, branchId);
        
        Map<String, long[]> roomStats = new HashMap<>(); // RoomName -> [totalSold, totalCapacity]
        for (com.example.cinema.model.entity.Showtime s : showtimes) {
            if (s.getRoom() == null) continue;
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
        BigDecimal comboRevenue = bookingRepository.calculateComboRevenue(start, end, branchId);
        if (comboRevenue == null) comboRevenue = BigDecimal.ZERO;

        return new StatisticsResponse.Builder()
                .totalRevenue(totalRevenue)
                .ticketRevenue(totalRevenue.subtract(comboRevenue))
                .comboRevenue(comboRevenue)
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

    @Override
    public CustomerStatisticsResponse getCustomerStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue(start, end, branchId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        CustomerStatisticsResponse res = new CustomerStatisticsResponse();
        res.setTotalCustomers(customerRepository.count());
        res.setNewCustomers(customerRepository.countNewCustomers(start, end, branchId));
        
        Map<String, Long> tiers = new HashMap<>();
        List<Object[]> tierData = customerRepository.countByMembershipTier();
        for (Object[] row : tierData) {
            if (row[0] != null && row[1] != null) {
                tiers.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        res.setMembershipLevels(tiers);

        List<com.example.cinema.model.entity.Customer> customers = customerRepository.findTopCustomersBySpend(start, end, branchId, org.springframework.data.domain.PageRequest.of(0, 10));
        List<CustomerStatisticsResponse.TopCustomer> topCustomers = customers.stream().map(c -> {
            CustomerStatisticsResponse.TopCustomer tc = new CustomerStatisticsResponse.TopCustomer();
            tc.setName(c.getFullName() != null ? c.getFullName() : c.getUser().getUsername());
            tc.setEmail(c.getEmail() != null ? c.getEmail() : c.getUser().getEmail());
            tc.setLevel(c.getMembershipLevel() != null ? c.getMembershipLevel().getName() : "GUEST");
            tc.setVisitCount((int) ((Number) customerRepository.countVisitsByCustomer(c.getId(), branchId)).longValue());
            BigDecimal spend = customerRepository.calculateTotalSpendByCustomer(c.getId(), branchId);
            tc.setTotalSpend(spend != null ? spend : BigDecimal.ZERO);
            tc.setPoints(c.getPoints());
            tc.setFavoriteGenre("Hành động");
            tc.setLastVisit("Vừa xong");
            return tc;
        }).collect(java.util.stream.Collectors.toList());
        res.setTopCustomers(topCustomers);

        res.setReturnRate(0.0); // Cần logic phân tích khách hàng quay lại
        res.setAvgSpend(res.getTotalCustomers() > 0 ? totalRevenue.divide(BigDecimal.valueOf(res.getTotalCustomers()), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
        res.setAgeDistribution(Collections.emptyMap());
        res.setGenderSplit(Collections.emptyMap());
        res.setVisitFrequency(Collections.emptyMap());
        res.setGenrePreferences(Collections.emptyMap());

        return res;
    }

    @Override
    public FnBStatisticsResponse getFnBStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        FnBStatisticsResponse res = new FnBStatisticsResponse();
        BigDecimal comboRevenue = bookingRepository.calculateComboRevenue(start, end, branchId);
        res.setTotalRevenue(comboRevenue != null ? comboRevenue : BigDecimal.ZERO);
        res.setTotalOrders(bookingRepository.countFnBOrders(start, end, branchId));
        
        long totalOrders = bookingRepository.countOrders(start, end, branchId);
        res.setAttachRate(totalOrders > 0 ? (double) res.getTotalOrders() / totalOrders * 100 : 0);
        res.setAvgOrderValue(res.getTotalOrders() > 0 ? res.getTotalRevenue().divide(BigDecimal.valueOf(res.getTotalOrders()), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);

        List<Object[]> topData = comboRepository.findTopSellingCombos(start, end, branchId, org.springframework.data.domain.PageRequest.of(0, 6));
        List<FnBStatisticsResponse.ProductStats> topProducts = topData.stream().map(row -> {
            String name = row[0] != null ? row[0].toString() : "Unknown Product";
            int quantity = row[1] != null ? ((Number) row[1]).intValue() : 0;
            BigDecimal revenue = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            return new FnBStatisticsResponse.ProductStats(name, quantity, revenue);
        }).collect(java.util.stream.Collectors.toList());
        res.setTopProducts(topProducts);

        res.setCategoryRevenue(Collections.emptyMap());
        res.setStockStatus(Collections.emptyList());
        
        return res;
    }

    @Override
    public PromotionStatisticsResponse getPromotionStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        PromotionStatisticsResponse res = new PromotionStatisticsResponse();
        List<Object[]> promoData = promotionRepository.calculatePromotionStats(start, end, branchId);
        
        long totalUsed = 0;
        List<PromotionStatisticsResponse.CampaignStats> campaigns = new java.util.ArrayList<>();

        for (Object[] row : promoData) {
            String name = row[0] != null ? row[0].toString() : "Unknown Promotion";
            LocalDate sDate = row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : LocalDate.now();
            LocalDate eDate = row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : LocalDate.now();
            long used = row[3] != null ? ((Number) row[3]).longValue() : 0;
            BigDecimal revenue = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            
            totalUsed += used;
            campaigns.add(new PromotionStatisticsResponse.CampaignStats(
                name, sDate.toString(), eDate.toString(), "active", used * 2, used, revenue, 100.0
            ));
        }

        res.setTotalUsed(totalUsed);
        res.setTotalIssued(totalUsed * 2);
        res.setTotalDiscount(BigDecimal.ZERO); // Cần query chi tiết giảm giá
        res.setAvgROI(0.0);
        res.setCampaigns(campaigns);
        res.setVoucherStatus(Collections.emptyMap());
        
        return res;
    }

    @Override
    public StaffStatisticsResponse getStaffStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);
        StaffStatisticsResponse res = new StaffStatisticsResponse();
        List<com.example.cinema.model.entity.User> staff = userRepository.findAllByRoleAndBranchAndStatusTrue(com.example.cinema.model.enums.Role.STAFF, branchId);
        res.setTotalStaff(staff.size());
        res.setTotalHours(0);
        res.setAttendanceRate(0.0);
        
        long totalTickets = bookingRepository.countTotalTickets(start, end, branchId);
        res.setAvgTicketsPerStaff(staff.isEmpty() ? 0 : (int)(totalTickets / staff.size()));

        List<StaffStatisticsResponse.StaffPerformance> topStaff = staff.stream().limit(5).map(s -> {
            StaffStatisticsResponse.StaffPerformance sp = new StaffStatisticsResponse.StaffPerformance();
            sp.setName(s.getUsername());
            sp.setRole("Staff");
            sp.setHours(0);
            sp.setTickets(0);
            sp.setRevenue(BigDecimal.ZERO);
            return sp;
        }).collect(java.util.stream.Collectors.toList());
        res.setTopStaff(topStaff);

        // Populate missing breakdown data for charts
        res.setAttendanceBreakdown(Collections.emptyMap());
        res.setShiftDistribution(Collections.emptyMap());
        res.setDailyStats(Collections.emptyMap());
        res.setStaffDetails(Collections.emptyList());

        return res;
    }

    @Override
    public TicketStatisticsResponse getTicketStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        LocalDateTime start = startDate.atTime(startTime);
        LocalDateTime end = endDate.atTime(endTime);

        TicketStatisticsResponse res = new TicketStatisticsResponse();
        res.setTotalSold(bookingRepository.countTotalTickets(start, end, branchId));
        
        List<Object[]> statusData = bookingRepository.countTicketsByStatus(start, end, branchId);
        long cancelled = 0;
        for (Object[] row : statusData) {
            if (row[0] != null && "CANCELLED".equals(row[0].toString()) && row[1] != null) {
                cancelled = ((Number) row[1]).longValue();
            }
        }
        res.setTotalCancelled(cancelled);
        res.setTotalRefunded(0);
        res.setNoShowRate(0.0);
        res.setRefundAmount(BigDecimal.ZERO);
        res.setNoShowCount(0);

        Map<String, Integer> channelSplit = new HashMap<>();
        List<Object[]> channelData = bookingRepository.countBookingsByChannel(start, end, branchId);
        long totalOrders = 0;
        for (Object[] row : channelData) totalOrders += ((Number) row[1]).longValue();
        for (Object[] row : channelData) {
            String method = row[0] != null ? row[0].toString() : "Khác";
            String channel = method.equals("CASH") ? "Quầy vé" : "Online App";
            long count = ((Number) row[1]).longValue();
            int current = channelSplit.getOrDefault(channel, 0);
            channelSplit.put(channel, current + (int)(count * 100 / (totalOrders > 0 ? totalOrders : 1)));
        }
        res.setTicketsByChannel(channelSplit);

        Map<String, TicketStatisticsResponse.SeatTypeStat> seatStats = new HashMap<>();
        List<Object[]> seatData = bookingRepository.calculateStatsBySeatType(start, end, branchId);
        for (Object[] row : seatData) {
            String typeName = row[0] != null ? row[0].toString() : "Unknown";
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
            BigDecimal revenue = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            seatStats.put(typeName, new TicketStatisticsResponse.SeatTypeStat(count, revenue));
        }
        res.setSeatTypeStats(seatStats);

        Map<String, Map<String, Long>> ticketTrend = new TreeMap<>();
        List<Object[]> trendData = bookingRepository.calculateTicketTrend(start, end, branchId);
        for (Object[] row : trendData) {
            if (row[0] == null) continue;
            String date = row[0].toString();
            Map<String, Long> vals = new HashMap<>();
            vals.put("sold", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            vals.put("cancelled", row[2] != null ? ((Number) row[2]).longValue() : 0L);
            ticketTrend.put(date, vals);
        }
        res.setTicketsByDay(ticketTrend);

        Map<String, Integer> statusBreakdown = new HashMap<>();
        res.setStatusBreakdown(Collections.emptyMap());

        List<Booking> recentBookings = bookingRepository.findRecentBookings(branchId, org.springframework.data.domain.PageRequest.of(0, 10));
        res.setRecentTickets(recentBookings.stream().map(b -> {
            TicketStatisticsResponse.RecentTicket rt = new TicketStatisticsResponse.RecentTicket();
            rt.setId(b.getBookingCode());
            rt.setCustomerName(b.getCustomer() != null ? b.getCustomer().getFullName() : "Guest");
            rt.setMovieTitle(b.getShowtime().getMovie().getTitle());
            rt.setShowtime(b.getShowtime().getStartTime().toString());
            rt.setChannel(b.getPayment() != null ? (b.getPayment().getPaymentMethod().toString().equals("CASH") ? "Quầy" : "Online") : "N/A");
            rt.setPrice(b.getTotalPrice());
            rt.setStatus(b.getStatus().toString());
            return rt;
        }).collect(Collectors.toList()));

        return res;
    }
}
