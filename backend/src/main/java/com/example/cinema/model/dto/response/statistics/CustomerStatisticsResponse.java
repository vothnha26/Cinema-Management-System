package com.example.cinema.model.dto.response.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CustomerStatisticsResponse {
    private long totalCustomers;
    private long newCustomers;
    private double returnRate;
    private BigDecimal avgSpend;
    private Map<String, Long> membershipLevels;
    private Map<String, Integer> ageDistribution;
    private Map<String, Integer> genderSplit;
    private Map<String, Integer> visitFrequency;
    private Map<String, Map<String, Long>> customerTrend; // Date -> {new: X, returning: Y}
    private Map<String, Integer> genrePreferences;
    private List<TopCustomer> topCustomers;

    public static class TopCustomer {
        private String name;
        private String email;
        private String level;
        private int visitCount;
        private BigDecimal totalSpend;
        private String favoriteGenre;
        private int points;
        private String lastVisit;

        public TopCustomer() {}

        public TopCustomer(String name, String email, String level, int visitCount, BigDecimal totalSpend, String favoriteGenre, int points, String lastVisit) {
            this.name = name;
            this.email = email;
            this.level = level;
            this.visitCount = visitCount;
            this.totalSpend = totalSpend;
            this.favoriteGenre = favoriteGenre;
            this.points = points;
            this.lastVisit = lastVisit;
        }

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public int getVisitCount() { return visitCount; }
        public void setVisitCount(int visitCount) { this.visitCount = visitCount; }
        public BigDecimal getTotalSpend() { return totalSpend; }
        public void setTotalSpend(BigDecimal totalSpend) { this.totalSpend = totalSpend; }
        public String getFavoriteGenre() { return favoriteGenre; }
        public void setFavoriteGenre(String favoriteGenre) { this.favoriteGenre = favoriteGenre; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getLastVisit() { return lastVisit; }
        public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }
    }

    // Getters/Setters
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getNewCustomers() { return newCustomers; }
    public void setNewCustomers(long newCustomers) { this.newCustomers = newCustomers; }
    public double getReturnRate() { return returnRate; }
    public void setReturnRate(double returnRate) { this.returnRate = returnRate; }
    public BigDecimal getAvgSpend() { return avgSpend; }
    public void setAvgSpend(BigDecimal avgSpend) { this.avgSpend = avgSpend; }
    public Map<String, Long> getMembershipLevels() { return membershipLevels; }
    public void setMembershipLevels(Map<String, Long> membershipLevels) { this.membershipLevels = membershipLevels; }
    public Map<String, Integer> getAgeDistribution() { return ageDistribution; }
    public void setAgeDistribution(Map<String, Integer> ageDistribution) { this.ageDistribution = ageDistribution; }
    public Map<String, Integer> getGenderSplit() { return genderSplit; }
    public void setGenderSplit(Map<String, Integer> genderSplit) { this.genderSplit = genderSplit; }
    public Map<String, Integer> getVisitFrequency() { return visitFrequency; }
    public void setVisitFrequency(Map<String, Integer> visitFrequency) { this.visitFrequency = visitFrequency; }
    public Map<String, Map<String, Long>> getCustomerTrend() { return customerTrend; }
    public void setCustomerTrend(Map<String, Map<String, Long>> customerTrend) { this.customerTrend = customerTrend; }
    public Map<String, Integer> getGenrePreferences() { return genrePreferences; }
    public void setGenrePreferences(Map<String, Integer> genrePreferences) { this.genrePreferences = genrePreferences; }
    public List<TopCustomer> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<TopCustomer> topCustomers) { this.topCustomers = topCustomers; }
}
