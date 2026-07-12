package com.example.cinema.service.analytics.provider;

import java.time.LocalDate;
import java.time.LocalTime;

public interface IStatisticsProvider<T> {
    String getProviderType();
    T getStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
}
