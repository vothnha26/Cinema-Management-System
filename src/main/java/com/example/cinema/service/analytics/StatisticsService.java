package com.example.cinema.service.analytics;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import java.time.LocalDate;
import java.time.LocalTime;

public interface StatisticsService {
    StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
    CustomerStatisticsResponse getCustomerStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
    FnBStatisticsResponse getFnBStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
    PromotionStatisticsResponse getPromotionStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
    StaffStatisticsResponse getStaffStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
    TicketStatisticsResponse getTicketStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId);
}
