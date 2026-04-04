package com.example.cinema.model.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SchedulingRequest {
    private LocalDate date;
    private String mode; // OVERWRITE, FILL
    private Double ratio;
    private LocalTime startTime;
    private LocalTime endTime;
    private String strategy; // BALANCED, REVENUE, CUSTOM
    private List<SchedulingRule> rules;

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
    public List<SchedulingRule> getRules() { return rules; }
    public void setRules(List<SchedulingRule> rules) { this.rules = rules; }
}
