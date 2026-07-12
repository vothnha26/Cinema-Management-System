package com.example.cinema.service.ai.strategy.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.ai.strategy.WeightingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

/**
 * Ưu tiên phim theo quốc gia sản xuất (ví dụ: Việt Nam).
 */
@Component
@Order(20)
public class OriginWeightingStrategy implements WeightingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime time, Map<Long, Double> buzzScores, 
                                 SchedulingRequest request, Map<Long, Integer> usageCount, 
                                 Map<Long, Integer> branchPriorities) {
        
        double boost = 0.0;
        
        // Ưu tiên phim Việt Nam (VN)
        if ("VN".equalsIgnoreCase(movie.getOriginCountry())) {
            boost += 50.0; // Cộng thêm điểm ưu tiên cơ bản cho phim Việt
            
            // Nếu người dùng yêu cầu cụ thể "Ưu tiên phim Việt" trong customDirectives
            if (request.getCustomDirectives() != null && 
                request.getCustomDirectives().toLowerCase().contains("phim việt")) {
                boost += 50.0; // Tăng gấp đôi độ ưu tiên
            }
        }
        
        return boost;
    }
}
