package com.example.cinema.service.ai;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.time.LocalDate;
import java.util.List;

public interface SchedulingService {
    List<ShowtimeResponse> getSuggestions(LocalDate date, String mode);
    List<ShowtimeResponse> getSuggestions(SchedulingRequest request);
    void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite);
}
