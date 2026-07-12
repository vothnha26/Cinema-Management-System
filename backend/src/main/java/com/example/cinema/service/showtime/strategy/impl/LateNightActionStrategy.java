package com.example.cinema.service.showtime.strategy.impl;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.service.showtime.strategy.SchedulingStrategy;
import java.time.LocalTime;
import java.util.Map;

public class LateNightActionStrategy implements SchedulingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime currentTime, Map<Long, Double> buzzScores) {
        double base = buzzScores.getOrDefault(movie.getId(), 0.0) + (movie.getRating() * 2);
        
        // Nếu là buổi đêm (> 21:00) và phim là Hành động/Kinh dị
        if (currentTime.isAfter(LocalTime.of(21, 0))) {
            boolean isLateType = movie.getGenres().stream()
                .anyMatch(g -> g.getName().contains("Hành động") || g.getName().contains("Kinh dị"));
            if (isLateType) return base + 100.0; 
        }
        
        return base;
    }

    @Override
    public String getName() { return "LATE_NIGHT"; }
}
