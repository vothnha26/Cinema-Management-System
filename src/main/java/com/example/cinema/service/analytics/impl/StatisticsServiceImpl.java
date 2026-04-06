package com.example.cinema.service.analytics.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
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
        BigDecimal comboRevenue = bookingRepository.calculateComboRevenue(start, end);
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
    public CustomerStatisticsResponse getCustomerStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        CustomerStatisticsResponse res = new CustomerStatisticsResponse();
        res.setTotalCustomers(customerRepository.count());
        res.setNewCustomers(customerRepository.countNewCustomers(start, end));
        
        // Membership tiers
        Map<String, Long> tiers = new HashMap<>();
        List<Object[]> tierData = customerRepository.countByMembershipTier();
        for (Object[] row : tierData) {
            tiers.put(row[0].toString(), (Long) row[1]);
        }
        res.setMembershipTiers(tiers);

        // Top Customers
        List<com.example.cinema.model.entity.Customer> customers = customerRepository.findTopCustomersBySpend(start, end, org.springframework.data.domain.PageRequest.of(0, 10));
        List<CustomerStatisticsResponse.TopCustomer> topCustomers = customers.stream().map(c -> {
            CustomerStatisticsResponse.TopCustomer tc = new CustomerStatisticsResponse.TopCustomer();
            tc.setName(c.getFullName() != null ? c.getFullName() : c.getUser().getUsername());
            tc.setEmail(c.getEmail() != null ? c.getEmail() : c.getUser().getEmail());
            tc.setTier(c.getMembershipTier().toString());
            tc.setVisitCount((int) customerRepository.countVisitsByCustomer(c.getId()));
            BigDecimal spend = customerRepository.calculateTotalSpendByCustomer(c.getId());
            tc.setTotalSpend(spend != null ? spend : BigDecimal.ZERO);
            tc.setPoints(c.getPoints());
            tc.setFavoriteGenre("Hành động"); // Mock
            tc.setLastVisit("Vừa xong"); // Mock
            return tc;
        }).collect(java.util.stream.Collectors.toList());
        res.setTopCustomers(topCustomers);

        // Mocks for others to match frontend UI
        res.setReturnRate(64.0);
        res.setAvgSpend(BigDecimal.valueOf(285000));
        res.setAgeDistribution(Map.of("18-24", 32, "25-34", 28, "35-44", 18, "45-54", 9));
        res.setGenderSplit(Map.of("Nam", 48, "Nữ", 44, "Khác", 8));
        res.setVisitFrequency(Map.of("1 lần", 40, "2-3 lần", 30, "4-6 lần", 18, "7-10 lần", 8));
        res.setGenrePreferences(Map.of("Hành động", 35, "Tình cảm", 22, "Hoạt hình", 18));

        return res;
    }

    @Override
    public FnBStatisticsResponse getFnBStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        FnBStatisticsResponse res = new FnBStatisticsResponse();
        BigDecimal comboRevenue = bookingRepository.calculateComboRevenue(start, end);
        res.setTotalRevenue(comboRevenue != null ? comboRevenue : BigDecimal.ZERO);
        res.setTotalOrders(bookingRepository.countFnBOrders(start, end));
        
        long totalOrders = bookingRepository.countOrders(start, end);
        res.setAttachRate(totalOrders > 0 ? (double) res.getTotalOrders() / totalOrders * 100 : 0);
        res.setAvgOrderValue(res.getTotalOrders() > 0 ? res.getTotalRevenue().divide(BigDecimal.valueOf(res.getTotalOrders()), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Top products
        List<Object[]> topData = comboRepository.findTopSellingCombos(start, end, org.springframework.data.domain.PageRequest.of(0, 6));
        List<FnBStatisticsResponse.ProductStats> topProducts = topData.stream().map(row -> 
            new FnBStatisticsResponse.ProductStats((String) row[0], ((Number) row[1]).intValue(), (BigDecimal) row[2])
        ).collect(java.util.stream.Collectors.toList());
        res.setTopProducts(topProducts);

        // Mocks for others
        res.setCategoryRevenue(Map.of("Bắp", 42.0, "Combo", 28.0, "Nước uống", 18.0, "Snack", 12.0));
        res.setStockStatus(List.of(
            new FnBStatisticsResponse.StockStatus("Bắp ngô", 15, 100, "kg"),
            new FnBStatisticsResponse.StockStatus("Pepsi", 48, 200, "lon")
        ));
        
        return res;
    }

    @Override
    public PromotionStatisticsResponse getPromotionStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        PromotionStatisticsResponse res = new PromotionStatisticsResponse();
        List<Object[]> promoData = promotionRepository.calculatePromotionStats(start, end);
        
        long totalUsed = 0;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<PromotionStatisticsResponse.CampaignStats> campaigns = new java.util.ArrayList<>();

        for (Object[] row : promoData) {
            String name = (String) row[0];
            LocalDate sDate = (LocalDate) row[1];
            LocalDate eDate = (LocalDate) row[2];
            long used = (Long) row[3];
            BigDecimal revenue = (BigDecimal) row[4];
            
            totalUsed += used;
            campaigns.add(new PromotionStatisticsResponse.CampaignStats(
                name, sDate.toString(), eDate.toString(), "active", used * 2, used, revenue, 320.0
            ));
        }

        res.setTotalUsed(totalUsed);
        res.setTotalIssued(totalUsed * 2); // Mock ratio
        res.setTotalDiscount(BigDecimal.valueOf(totalUsed * 50000)); // Mock discount
        res.setAvgROI(320.0);
        res.setCampaigns(campaigns);
        res.setVoucherStatus(Map.of("Đã sử dụng", 65.0, "Chưa dùng", 22.0, "Hết hạn", 13.0));
        
        return res;
    }

    @Override
    public StaffStatisticsResponse getStaffStatistics(LocalDate startDate, LocalDate endDate) {
        StaffStatisticsResponse res = new StaffStatisticsResponse();
        List<com.example.cinema.model.entity.User> staff = userRepository.findAllByRoleAndStatusTrue(com.example.cinema.model.enums.Role.STAFF);
        res.setTotalStaff(staff.size());
        res.setTotalHours(staff.size() * 160); // Mock
        res.setAttendanceRate(92.0);
        res.setAvgTicketsPerStaff(236);

        List<StaffStatisticsResponse.StaffPerformance> topStaff = staff.stream().limit(5).map(s -> {
            StaffStatisticsResponse.StaffPerformance sp = new StaffStatisticsResponse.StaffPerformance();
            sp.setName(s.getUsername());
            sp.setRole("Staff");
            sp.setHours(160);
            sp.setTickets(200);
            sp.setRevenue(BigDecimal.valueOf(20000000));
            return sp;
        }).collect(java.util.stream.Collectors.toList());
        res.setTopStaff(topStaff);

        return res;
    }

    @Override
    public TicketStatisticsResponse getTicketStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        TicketStatisticsResponse res = new TicketStatisticsResponse();
        res.setTotalSold(bookingRepository.countTotalTickets());
        
        List<Object[]> statusData = bookingRepository.countTicketsByStatus(start, end);
        long cancelled = 0;
        for (Object[] row : statusData) {
            if ("CANCELLED".equals(row[0].toString())) cancelled = (Long) row[1];
        }
        res.setTotalCancelled(cancelled);
        res.setTotalRefunded(cancelled / 2);
        res.setNoShowRate(3.2);
        res.setRefundAmount(BigDecimal.valueOf(cancelled * 90000));

        // Seat type stats
        Map<String, TicketStatisticsResponse.SeatTypeStat> seatStats = new HashMap<>();
        List<Object[]> seatData = bookingRepository.calculateStatsBySeatType(start, end);
        for (Object[] row : seatData) {
            seatStats.put(row[0].toString(), new TicketStatisticsResponse.SeatTypeStat((Long) row[1], (BigDecimal) row[2]));
        }
        res.setSeatTypeStats(seatStats);

        res.setTicketsByChannel(Map.of("Online App", 48, "Website", 27, "Quầy vé", 18, "Đối tác", 7));
        res.setStatusBreakdown(Map.of("Đã bán", 82, "Đã hủy", 10, "Hoàn tiền", 4, "No-Show", 4));

        return res;
    }
}
