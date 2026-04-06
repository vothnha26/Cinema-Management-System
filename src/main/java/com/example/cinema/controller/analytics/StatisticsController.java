package com.example.cinema.controller.analytics;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import com.example.cinema.service.analytics.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getOverview(startDate, endDate)));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerStatisticsResponse>> getCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getCustomerStatistics(startDate, endDate)));
    }

    @GetMapping("/fnb")
    public ResponseEntity<ApiResponse<FnBStatisticsResponse>> getFnB(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getFnBStatistics(startDate, endDate)));
    }

    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<PromotionStatisticsResponse>> getPromotions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getPromotionStatistics(startDate, endDate)));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<StaffStatisticsResponse>> getStaff(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getStaffStatistics(startDate, endDate)));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketStatisticsResponse>> getTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getTicketStatistics(startDate, endDate)));
    }
}
