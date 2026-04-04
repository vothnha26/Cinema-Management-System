package com.example.cinema.service.showtime;

import com.example.cinema.model.dto.response.ShowtimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SchedulingService {
    List<ShowtimeResponse> generateAISuggestions(LocalDate targetDate, String mode, double topRatio, LocalTime startTime, LocalTime endTime);

    void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite);
}
