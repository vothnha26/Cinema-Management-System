package com.example.cinema.service.analytics;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import java.time.LocalDate;

public interface StatisticsService {
    StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate);
    CustomerStatisticsResponse getCustomerStatistics(LocalDate startDate, LocalDate endDate);
    FnBStatisticsResponse getFnBStatistics(LocalDate startDate, LocalDate endDate);
    PromotionStatisticsResponse getPromotionStatistics(LocalDate startDate, LocalDate endDate);
    StaffStatisticsResponse getStaffStatistics(LocalDate startDate, LocalDate endDate);
    TicketStatisticsResponse getTicketStatistics(LocalDate startDate, LocalDate endDate);
}
