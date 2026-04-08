package com.example.cinema.controller.analytics;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.StatisticsResponse;
import com.example.cinema.model.dto.response.statistics.*;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.entity.Staff;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.service.analytics.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final StaffRepository staffRepository;
    private final com.example.cinema.repository.user.UserRepository userRepository;

    public StatisticsController(StatisticsService statisticsService, StaffRepository staffRepository, com.example.cinema.repository.user.UserRepository userRepository) {
        this.statisticsService = statisticsService;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    private Long resolveBranchId(Long requestBranchId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        if (user.getRole().name().equals("ADMIN")) {
            return requestBranchId;
        }
        
        // Manager/Staff only see their own branch
        Optional<Staff> staff = staffRepository.findByUserId(user.getId());
        return staff.map(com.example.cinema.model.entity.Staff::getBranch)
                    .map(com.example.cinema.model.entity.Branch::getId)
                    .orElse(null);
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getOverview(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerStatisticsResponse>> getCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getCustomerStatistics(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }

    @GetMapping("/fnb")
    public ResponseEntity<ApiResponse<FnBStatisticsResponse>> getFnB(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getFnBStatistics(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }

    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<PromotionStatisticsResponse>> getPromotions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getPromotionStatistics(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<StaffStatisticsResponse>> getStaff(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getStaffStatistics(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketStatisticsResponse>> getTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long branchId) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        if (startTime == null) startTime = LocalTime.MIN;
        if (endTime == null) endTime = LocalTime.MAX;
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getTicketStatistics(startDate, endDate, startTime, endTime, resolveBranchId(branchId))));
    }
}
