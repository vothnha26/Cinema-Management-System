package com.example.cinema.model.dto.response.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TicketStatisticsResponse {
    private long totalSold;
    private long totalCancelled;
    private long totalRefunded;
    private double noShowRate;
    private BigDecimal refundAmount;
    private long noShowCount;
    private Map<String, Integer> ticketsByChannel; // Channel -> Percentage
    private Map<String, SeatTypeStat> seatTypeStats; // SeatType -> Stats
    private Map<String, Map<String, Long>> ticketsByDay; // Date -> {sold: X, cancelled: Y}
    private Map<String, Integer> statusBreakdown; // Status -> Percentage
    private List<RecentTicket> recentTickets;

    public static class SeatTypeStat {
        private long count;
        private BigDecimal revenue;

        public SeatTypeStat() {}
        public SeatTypeStat(long count, BigDecimal revenue) {
            this.count = count;
            this.revenue = revenue;
        }

        // Getters/Setters
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class RecentTicket {
        private String id;
        private String customerName;
        private String movieTitle;
        private String showtime;
        private String seatType;
        private String channel;
        private BigDecimal price;
        private String status;

        public RecentTicket() {}

        // Getters/Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getMovieTitle() { return movieTitle; }
        public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
        public String getShowtime() { return showtime; }
        public void setShowtime(String showtime) { this.showtime = showtime; }
        public String getSeatType() { return seatType; }
        public void setSeatType(String seatType) { this.seatType = seatType; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Getters/Setters
    public long getTotalSold() { return totalSold; }
    public void setTotalSold(long totalSold) { this.totalSold = totalSold; }
    public long getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(long totalCancelled) { this.totalCancelled = totalCancelled; }
    public long getTotalRefunded() { return totalRefunded; }
    public void setTotalRefunded(long totalRefunded) { this.totalRefunded = totalRefunded; }
    public double getNoShowRate() { return noShowRate; }
    public void setNoShowRate(double noShowRate) { this.noShowRate = noShowRate; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public long getNoShowCount() { return noShowCount; }
    public void setNoShowCount(long noShowCount) { this.noShowCount = noShowCount; }
    public Map<String, Integer> getTicketsByChannel() { return ticketsByChannel; }
    public void setTicketsByChannel(Map<String, Integer> ticketsByChannel) { this.ticketsByChannel = ticketsByChannel; }
    public Map<String, SeatTypeStat> getSeatTypeStats() { return seatTypeStats; }
    public void setSeatTypeStats(Map<String, SeatTypeStat> seatTypeStats) { this.seatTypeStats = seatTypeStats; }
    public Map<String, Map<String, Long>> getTicketsByDay() { return ticketsByDay; }
    public void setTicketsByDay(Map<String, Map<String, Long>> ticketsByDay) { this.ticketsByDay = ticketsByDay; }
    public Map<String, Integer> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Integer> statusBreakdown) { this.statusBreakdown = statusBreakdown; }
    public List<RecentTicket> getRecentTickets() { return recentTickets; }
    public void setRecentTickets(List<RecentTicket> recentTickets) { this.recentTickets = recentTickets; }
}
