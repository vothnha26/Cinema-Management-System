package com.example.cinema.model.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class StatisticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal ticketRevenue;
    private BigDecimal comboRevenue;
    private Long totalTicketsSold;
    private Long newCustomersCount;
    private Double averageOccupancyRate;
    private Map<String, BigDecimal> revenueByMovie; // Movie Title -> Amount
    private Map<String, BigDecimal> revenueByDay;   // Date -> Amount
    private Map<String, Long> ticketsByDay;         // Date -> Count
    private Map<Integer, BigDecimal> revenueByHour; // Hour (0-23) -> Amount

    public StatisticsResponse() {
    }

    public static class Builder {
        private StatisticsResponse response = new StatisticsResponse();

        public Builder totalRevenue(BigDecimal total) {
            response.totalRevenue = total;
            return this;
        }

        public Builder ticketRevenue(BigDecimal ticket) {
            response.ticketRevenue = ticket;
            return this;
        }

        public Builder comboRevenue(BigDecimal combo) {
            response.comboRevenue = combo;
            return this;
        }

        public Builder totalTickets(Long count) {
            response.totalTicketsSold = count;
            return this;
        }

        public Builder newCustomers(Long count) {
            response.newCustomersCount = count;
            return this;
        }

        public Builder occupancyRate(Double rate) {
            response.averageOccupancyRate = rate;
            return this;
        }

        public Builder revenueByMovie(Map<String, BigDecimal> data) {
            response.revenueByMovie = data;
            return this;
        }

        public Builder revenueByDay(Map<String, BigDecimal> data) {
            response.revenueByDay = data;
            return this;
        }

        public Builder ticketsByDay(Map<String, Long> data) {
            response.ticketsByDay = data;
            return this;
        }

        public Builder revenueByHour(Map<Integer, BigDecimal> data) {
            response.revenueByHour = data;
            return this;
        }

        public StatisticsResponse build() {
            return response;
        }
    }

    // Getters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getTicketRevenue() { return ticketRevenue; }
    public BigDecimal getComboRevenue() { return comboRevenue; }
    public Long getTotalTicketsSold() { return totalTicketsSold; }
    public Long getNewCustomersCount() { return newCustomersCount; }
    public Double getAverageOccupancyRate() { return averageOccupancyRate; }
    public Map<String, BigDecimal> getRevenueByMovie() { return revenueByMovie; }
    public Map<String, BigDecimal> getRevenueByDay() { return revenueByDay; }
    public Map<String, Long> getTicketsByDay() { return ticketsByDay; }
    public Map<Integer, BigDecimal> getRevenueByHour() { return revenueByHour; }
}
