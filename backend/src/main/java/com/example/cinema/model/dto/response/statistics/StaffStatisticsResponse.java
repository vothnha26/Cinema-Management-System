package com.example.cinema.model.dto.response.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StaffStatisticsResponse {
    private int totalStaff;
    private int totalHours;
    private double attendanceRate;
    private int avgTicketsPerStaff;
    private List<StaffPerformance> topStaff;
    private Map<String, Integer> attendanceBreakdown; // Status -> Percentage
    private Map<String, Map<String, Object>> dailyStats; // Date -> {hours: X, revenue: Y}
    private Map<String, Integer> shiftDistribution; // Shift -> Percentage
    private List<StaffDetail> staffDetails;

    public static class StaffPerformance {
        private String name;
        private String role;
        private int hours;
        private int tickets;
        private BigDecimal revenue;

        public StaffPerformance() {}

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public int getHours() { return hours; }
        public void setHours(int hours) { this.hours = hours; }
        public int getTickets() { return tickets; }
        public void setTickets(int tickets) { this.tickets = tickets; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class StaffDetail {
        private String name;
        private String role;
        private int shifts;
        private int hours;
        private int tickets;
        private BigDecimal revenue;
        private double attendance;
        private int performance;
        private boolean isActive;

        public StaffDetail() {}

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public int getShifts() { return shifts; }
        public void setShifts(int shifts) { this.shifts = shifts; }
        public int getHours() { return hours; }
        public void setHours(int hours) { this.hours = hours; }
        public int getTickets() { return tickets; }
        public void setTickets(int tickets) { this.tickets = tickets; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public double getAttendance() { return attendance; }
        public void setAttendance(double attendance) { this.attendance = attendance; }
        public int getPerformance() { return performance; }
        public void setPerformance(int performance) { this.performance = performance; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    // Getters/Setters
    public int getTotalStaff() { return totalStaff; }
    public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }
    public int getTotalHours() { return totalHours; }
    public void setTotalHours(int totalHours) { this.totalHours = totalHours; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    public int getAvgTicketsPerStaff() { return avgTicketsPerStaff; }
    public void setAvgTicketsPerStaff(int avgTicketsPerStaff) { this.avgTicketsPerStaff = avgTicketsPerStaff; }
    public List<StaffPerformance> getTopStaff() { return topStaff; }
    public void setTopStaff(List<StaffPerformance> topStaff) { this.topStaff = topStaff; }
    public Map<String, Integer> getAttendanceBreakdown() { return attendanceBreakdown; }
    public void setAttendanceBreakdown(Map<String, Integer> attendanceBreakdown) { this.attendanceBreakdown = attendanceBreakdown; }
    public Map<String, Map<String, Object>> getDailyStats() { return dailyStats; }
    public void setDailyStats(Map<String, Map<String, Object>> dailyStats) { this.dailyStats = dailyStats; }
    public Map<String, Integer> getShiftDistribution() { return shiftDistribution; }
    public void setShiftDistribution(Map<String, Integer> shiftDistribution) { this.shiftDistribution = shiftDistribution; }
    public List<StaffDetail> getStaffDetails() { return staffDetails; }
    public void setStaffDetails(List<StaffDetail> staffDetails) { this.staffDetails = staffDetails; }
}
