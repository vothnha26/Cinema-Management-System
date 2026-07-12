package com.example.cinema.model.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public class SchedulingRequest {
    private LocalDate date;
    private String mode; // OVERWRITE, ADD
    private Double ratio;
    private LocalTime startTime;
    private LocalTime endTime;
    private String strategy; // BALANCED, REVENUE, CUSTOM
    private Integer cleanupTime;
    private String customDirectives;
    private Long branchId;
    private java.util.List<SchedulingRule> rules;

    public SchedulingRequest() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Double getRatio() { return ratio; }
    public void setRatio(Double ratio) { this.ratio = ratio; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public Integer getCleanupTime() { return cleanupTime; }
    public void setCleanupTime(Integer cleanupTime) { this.cleanupTime = cleanupTime; }
    public String getCustomDirectives() { return customDirectives; }
    public void setCustomDirectives(String customDirectives) { this.customDirectives = customDirectives; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public java.util.List<SchedulingRule> getRules() { return rules; }
    public void setRules(java.util.List<SchedulingRule> rules) { this.rules = rules; }
}
