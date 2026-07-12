package com.example.cinema.model.dto.response.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FnBStatisticsResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private double attachRate;
    private BigDecimal avgOrderValue;
    private List<ProductStats> topProducts;
    private Map<String, Double> categoryRevenue; // Category -> Percentage
    private Map<String, Map<String, BigDecimal>> revenueByDay; // Date -> {fnb: X, ticket: Y}
    private Map<Integer, Double> attachByHour; // Hour -> Percentage
    private List<StockStatus> stockStatus;

    public static class ProductStats {
        private String name;
        private int qty;
        private BigDecimal revenue;

        public ProductStats() {}
        public ProductStats(String name, int qty, BigDecimal revenue) {
            this.name = name;
            this.qty = qty;
            this.revenue = revenue;
        }

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class StockStatus {
        private String name;
        private int current;
        private int max;
        private String unit;

        public StockStatus() {}
        public StockStatus(String name, int current, int max, String unit) {
            this.name = name;
            this.current = current;
            this.max = max;
            this.unit = unit;
        }

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCurrent() { return current; }
        public void setCurrent(int current) { this.current = current; }
        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    // Getters/Setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public double getAttachRate() { return attachRate; }
    public void setAttachRate(double attachRate) { this.attachRate = attachRate; }
    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(BigDecimal avgOrderValue) { this.avgOrderValue = avgOrderValue; }
    public List<ProductStats> getTopProducts() { return topProducts; }
    public void setTopProducts(List<ProductStats> topProducts) { this.topProducts = topProducts; }
    public Map<String, Double> getCategoryRevenue() { return categoryRevenue; }
    public void setCategoryRevenue(Map<String, Double> categoryRevenue) { this.categoryRevenue = categoryRevenue; }
    public Map<String, Map<String, BigDecimal>> getRevenueByDay() { return revenueByDay; }
    public void setRevenueByDay(Map<String, Map<String, BigDecimal>> revenueByDay) { this.revenueByDay = revenueByDay; }
    public Map<Integer, Double> getAttachByHour() { return attachByHour; }
    public void setAttachByHour(Map<Integer, Double> attachByHour) { this.attachByHour = attachByHour; }
    public List<StockStatus> getStockStatus() { return stockStatus; }
    public void setStockStatus(List<StockStatus> stockStatus) { this.stockStatus = stockStatus; }
}
