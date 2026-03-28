package com.example.cinema.service;

import com.example.cinema.model.dto.response.StatisticsResponse;
import java.time.LocalDate;

public interface StatisticsService {
    StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate);
}
