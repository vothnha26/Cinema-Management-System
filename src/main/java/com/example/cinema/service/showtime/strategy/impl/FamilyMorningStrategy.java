package com.example.cinema.service.showtime.strategy.impl;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.service.showtime.strategy.SchedulingStrategy;
import java.time.LocalTime;
import java.util.Map;

public class FamilyMorningStrategy implements SchedulingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime currentTime, Map<Long, Double> buzzScores) {
        double base = buzzScores.getOrDefault(movie.getId(), 0.0) + (movie.getRating() * 2);
        
        // Nếu là buổi sáng (< 14:00) và phim là Hoạt hình/Gia đình
        if (currentTime.isBefore(LocalTime.of(14, 0))) {
            boolean isFamily = movie.getGenres().stream()
                .anyMatch(g -> g.getName().contains("Hoạt hình") || g.getName().contains("Gia đình"));
            if (isFamily) return base + 100.0; // Ưu tiên cực cao
        }
        
        return base;
    }

    @Override
    public String getName() { return "FAMILY"; }
}
