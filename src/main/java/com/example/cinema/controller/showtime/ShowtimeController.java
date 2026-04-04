package com.example.cinema.controller.showtime;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.service.showtime.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@CrossOrigin(origins = "*")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getAllShowtimes(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getAllShowtimes(date)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getAllShowtimes(null).stream()
                .filter(s -> s.getId().equals(id)).findFirst().orElse(null)));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<com.example.cinema.model.dto.response.SeatResponse>>> getSeats(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getSeatStatusForShowtime(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeResponse>> createShowtime(@RequestBody @Valid ShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(showtimeService.createShowtime(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> updateShowtime(@PathVariable Long id,
            @RequestBody @Valid ShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.updateShowtime(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
