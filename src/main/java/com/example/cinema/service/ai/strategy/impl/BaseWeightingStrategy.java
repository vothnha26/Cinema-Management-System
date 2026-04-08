package com.example.cinema.service.ai.strategy.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.ai.strategy.WeightingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

/**
 * Trọng số cơ bản dựa trên điểm Buzz, Rating và Độ ưu tiên tại chi nhánh.
 */
@Component
@Order(10)
public class BaseWeightingStrategy implements WeightingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime time, Map<Long, Double> buzzScores, 
                                 SchedulingRequest request, Map<Long, Integer> usageCount, 
                                 Map<Long, Integer> branchPriorities) {
        
        int branchPriority = branchPriorities.getOrDefault(movie.getId(), 1);
        double weight = buzzScores.getOrDefault(movie.getId(), 0.0) 
                     + (movie.getRating() * 2) 
                     + (branchPriority * 10.0);
        
        // Trừ trọng số dựa trên số lần sử dụng để đảm bảo tính đa dạng
        weight -= (usageCount.getOrDefault(movie.getId(), 0) * 20.0);
        
        return weight;
    }
}
