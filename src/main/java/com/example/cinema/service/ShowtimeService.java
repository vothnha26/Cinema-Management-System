package com.example.cinema.service;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getAllShowtimes();
    ShowtimeResponse createShowtime(ShowtimeRequest request);
    void deleteShowtime(Long id);
}
