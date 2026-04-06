package com.example.cinema.model.dto.response.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PromotionStatisticsResponse {
    private long totalIssued;
    private long totalUsed;
    private BigDecimal totalDiscount;
    private double avgROI;
    private List<CampaignStats> campaigns;
    private Map<String, Double> voucherStatus; // Status -> Percentage
    private Map<String, Map<String, BigDecimal>> discountByDay; // Date -> {discount: X, revenue: Y}
    private Map<String, Long> voucherTypes; // Type -> Count
    private List<VoucherDetail> voucherDetails;

    public static class CampaignStats {
        private String name;
        private String startDate;
        private String endDate;
        private String status;
        private long issued;
        private long used;
        private BigDecimal revenue;
        private double roi;

        public CampaignStats() {}
        public CampaignStats(String name, String startDate, String endDate, String status, long issued, long used, BigDecimal revenue, double roi) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.issued = issued;
            this.used = used;
            this.revenue = revenue;
            this.roi = roi;
        }

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getIssued() { return issued; }
        public void setIssued(long issued) { this.issued = issued; }
        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public double getRoi() { return roi; }
        public void setRoi(double roi) { this.roi = roi; }
    }

    public static class VoucherDetail {
        private String code;
        private String campaign;
        private String type;
        private BigDecimal discountValue;
        private boolean isPercent;
        private long issued;
        private long used;
        private BigDecimal revenueGenerated;
        private double roi;

        public VoucherDetail() {}

        // Getters/Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getCampaign() { return campaign; }
        public void setCampaign(String campaign) { this.campaign = campaign; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public boolean isPercent() { return isPercent; }
        public void setPercent(boolean percent) { isPercent = percent; }
        public long getIssued() { return issued; }
        public void setIssued(long issued) { this.issued = issued; }
        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }
        public BigDecimal getRevenueGenerated() { return revenueGenerated; }
        public void setRevenueGenerated(BigDecimal revenueGenerated) { this.revenueGenerated = revenueGenerated; }
        public double getRoi() { return roi; }
        public void setRoi(double roi) { this.roi = roi; }
    }

    // Getters/Setters
    public long getTotalIssued() { return totalIssued; }
    public void setTotalIssued(long totalIssued) { this.totalIssued = totalIssued; }
    public long getTotalUsed() { return totalUsed; }
    public void setTotalUsed(long totalUsed) { this.totalUsed = totalUsed; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    public double getAvgROI() { return avgROI; }
    public void setAvgROI(double avgROI) { this.avgROI = avgROI; }
    public List<CampaignStats> getCampaigns() { return campaigns; }
    public void setCampaigns(List<CampaignStats> campaigns) { this.campaigns = campaigns; }
    public Map<String, Double> getVoucherStatus() { return voucherStatus; }
    public void setVoucherStatus(Map<String, Double> voucherStatus) { this.voucherStatus = voucherStatus; }
    public Map<String, Map<String, BigDecimal>> getDiscountByDay() { return discountByDay; }
    public void setDiscountByDay(Map<String, Map<String, BigDecimal>> discountByDay) { this.discountByDay = discountByDay; }
    public Map<String, Long> getVoucherTypes() { return voucherTypes; }
    public void setVoucherTypes(Map<String, Long> voucherTypes) { this.voucherTypes = voucherTypes; }
    public List<VoucherDetail> getVoucherDetails() { return voucherDetails; }
    public void setVoucherDetails(List<VoucherDetail> voucherDetails) { this.voucherDetails = voucherDetails; }
}
