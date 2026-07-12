package com.example.cinema.service.showtime.strategy;

import com.example.cinema.model.entity.Movie;
import java.time.LocalTime;
import java.util.Map;

/**
 * Strategy Pattern: Định nghĩa các chiến lược xếp lịch khác nhau
 */
public interface SchedulingStrategy {
    /**
     * Tính toán trọng số của một bộ phim tại một thời điểm cụ thể.
     * Phim có trọng số cao hơn sẽ được ưu tiên xếp lịch trước.
     */
    double calculateWeight(Movie movie, LocalTime currentTime, Map<Long, Double> buzzScores);
    
    String getName();
}
