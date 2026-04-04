package com.example.cinema.controller.showtime;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.service.showtime.SchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody SchedulingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                schedulingService.generateAISuggestions(request)));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Void>> apply(
            @RequestBody List<ShowtimeResponse> suggestions,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        schedulingService.applySuggestions(suggestions, overwrite);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
