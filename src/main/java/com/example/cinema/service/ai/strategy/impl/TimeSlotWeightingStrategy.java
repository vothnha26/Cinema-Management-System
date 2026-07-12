package com.example.cinema.service.ai.strategy.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.ai.strategy.WeightingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

/**
 * Chiến lược ưu tiên phim theo khung giờ (Giờ vàng, Hoạt hình buổi sáng, Hành động ban đêm).
 */
@Component
@Order(40)
public class TimeSlotWeightingStrategy implements WeightingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime time, Map<Long, Double> buzzScores, 
                                 SchedulingRequest request, Map<Long, Integer> usageCount, 
                                 Map<Long, Integer> branchPriorities) {
        
        double boost = 0.0;
        
        if ("REVENUE".equalsIgnoreCase(request.getStrategy())) {
            // Giờ vàng (18:00 - 22:30)
            if (time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(22, 30))) {
                boost += 100.0; // Ưu tiên phim trong giờ vàng
            }
        } else if ("FAMILY".equalsIgnoreCase(request.getStrategy())) {
            // Buổi sáng & chiều sớm (trước 14:00) cho gia đình/trẻ em
            if (time.isBefore(LocalTime.of(14, 0))) {
                if (movie.getGenres().stream().anyMatch(g -> g.getName().contains("Hoạt hình") || g.getName().contains("Gia đình"))) {
                    boost += 100.0;
                }
            }
        } else if ("LATE_NIGHT".equalsIgnoreCase(request.getStrategy())) {
            // Buổi đêm (sau 21:00) cho phim hành động/kinh dị
            if (time.isAfter(LocalTime.of(21, 0))) {
                if (movie.getGenres().stream().anyMatch(g -> g.getName().contains("Hành động") || g.getName().contains("Kinh dị"))) {
                    boost += 100.0;
                }
            }
        }
        
        return boost;
    }
}
