package com.example.cinema.service.showtime;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getAllShowtimes();
    ShowtimeResponse createShowtime(ShowtimeRequest request);
    ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request);
    void deleteShowtime(Long id);
}
