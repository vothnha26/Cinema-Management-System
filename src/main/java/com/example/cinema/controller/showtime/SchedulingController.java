package com.example.cinema.controller.showtime;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.service.showtime.SchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
@CrossOrigin(origins = "*")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> suggest(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "OVERWRITE") String mode) {
        return ResponseEntity.ok(ApiResponse.ok(schedulingService.generateAISuggestions(date, mode)));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Void>> apply(
            @RequestBody List<ShowtimeResponse> suggestions,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        schedulingService.applySuggestions(suggestions, overwrite);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
