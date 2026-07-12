package com.example.cinema.controller.ai;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.service.ai.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @GetMapping("/suggest")
    public ApiResponse<List<ShowtimeResponse>> getSuggestions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "FILL") String mode) {
        return ApiResponse.ok(schedulingService.getSuggestions(date, mode));
    }

    @PostMapping("/suggest")
    public ApiResponse<List<ShowtimeResponse>> getSuggestionsPost(@RequestBody SchedulingRequest request) {
        return ApiResponse.ok(schedulingService.getSuggestions(request));
    }

    @PostMapping("/apply")
    public ApiResponse<Void> applySuggestions(@RequestBody List<ShowtimeResponse> suggestions, 
                                             @RequestParam(defaultValue = "false") boolean overwrite) {
        schedulingService.applySuggestions(suggestions, overwrite);
        return ApiResponse.ok(null);
    }
}
