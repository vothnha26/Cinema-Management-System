package com.example.cinema.service;

import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getShowtimesByMovie(Long movieId);
    List<SeatResponse> getSeatStatusForShowtime(Long showtimeId);
}
