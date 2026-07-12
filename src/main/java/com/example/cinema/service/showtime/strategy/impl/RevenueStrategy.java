package com.example.cinema.service.showtime.strategy.impl;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.showtime.strategy.SchedulingStrategy;
import java.time.LocalTime;
import java.util.Map;

public class RevenueStrategy implements SchedulingStrategy {
    @Override
    public double calculateWeight(Movie movie, LocalTime currentTime, Map<Long, Double> buzzScores) {
        double buzz = buzzScores.getOrDefault(movie.getId(), 0.0);
        int priority = movie.getPriorityLevel() != null ? movie.getPriorityLevel() : 1;
        
        // Công thức: Buzz + Priority trọng số cao
        return buzz + (priority * 15.0);
    }

    @Override
    public String getName() { return "REVENUE"; }
}
