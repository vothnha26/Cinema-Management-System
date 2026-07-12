package com.example.cinema.model.dto.request;

import java.time.LocalTime;

public class SchedulingRule {
    private LocalTime startTime;
    private LocalTime endTime;
    private String priorityType; // GENRE, PRIORITY, RATING, MOVIE
    private String targetValue;  // VD: "Hành động", "8", "101"
    private Double weightBoost;  // Hệ số ưu tiên (VD: 2.0)

    public SchedulingRule() {}

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getPriorityType() { return priorityType; }
    public void setPriorityType(String priorityType) { this.priorityType = priorityType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public Double getWeightBoost() { return weightBoost; }
    public void setWeightBoost(Double weightBoost) { this.weightBoost = weightBoost; }
}
