package com.example.cinema.service.ai.strategy.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.request.SchedulingRule;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.ai.strategy.WeightingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

/**
 * Ưu tiên phim dựa trên thể loại hoặc các quy tắc cụ thể do người dùng thiết lập.
 */
@Component
@Order(30)
public class GenreWeightingStrategy implements WeightingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime time, Map<Long, Double> buzzScores, 
                                 SchedulingRequest request, Map<Long, Integer> usageCount, 
                                 Map<Long, Integer> branchPriorities) {
        
        double weightAdjustment = 0.0;
        
        if (request.getRules() != null) {
            for (SchedulingRule rule : request.getRules()) {
                if (!time.isBefore(rule.getStartTime()) && time.isBefore(rule.getEndTime())) {
                    boolean match = false;
                    switch (rule.getPriorityType().toUpperCase()) {
                        case "GENRE":
                            match = movie.getGenres().stream().anyMatch(g -> g.getName().equalsIgnoreCase(rule.getTargetValue()));
                            break;
                        case "RATING":
                            match = movie.getRating() >= Double.parseDouble(rule.getTargetValue());
                            break;
                        case "MOVIE":
                            match = movie.getId().toString().equals(rule.getTargetValue());
                            break;
                    }
                    if (match) {
                        // Trả về giá trị điều chỉnh (ví dụ: nhân với 2.0 có nghĩa là cộng thêm một lượng điểm lớn)
                        double boost = rule.getWeightBoost() != null ? rule.getWeightBoost() : 2.0;
                        weightAdjustment += (100.0 * boost); 
                    }
                }
            }
        }
        
        return weightAdjustment;
    }
}
