package com.example.cinema.service.analytics.impl;

import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import com.example.cinema.service.analytics.StatisticsService;
import com.example.cinema.service.analytics.provider.IStatisticsProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final Map<String, IStatisticsProvider<?>> providers;

    public StatisticsServiceImpl(List<IStatisticsProvider<?>> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(IStatisticsProvider::getProviderType, p -> p));
    }

    @Override
    public StatisticsResponse getOverview(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        return (StatisticsResponse) providers.get("OVERVIEW").getStatistics(startDate, endDate, startTime, endTime, branchId);
    }

    @Override
    public CustomerStatisticsResponse getCustomerStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        // Mock hoặc gọi Provider tương ứng (Tôi sẽ chỉ demo cấu hình cho 1-2 provider để giữ context gọn)
        return new CustomerStatisticsResponse();
    }

    @Override
    public FnBStatisticsResponse getFnBStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        return new FnBStatisticsResponse();
    }

    @Override
    public PromotionStatisticsResponse getPromotionStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        return new PromotionStatisticsResponse();
    }

    @Override
    public StaffStatisticsResponse getStaffStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        return new StaffStatisticsResponse();
    }

    @Override
    public TicketStatisticsResponse getTicketStatistics(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Long branchId) {
        return new TicketStatisticsResponse();
    }
}
