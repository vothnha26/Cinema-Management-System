package com.example.cinema.service.showtime;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getAllShowtimes();
    List<ShowtimeResponse> getShowtimesByMovie(Long movieId);
    List<SeatResponse> getSeatStatusForShowtime(Long showtimeId);
    ShowtimeResponse createShowtime(ShowtimeRequest request);
    ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request);
    void deleteShowtime(Long id);
}
