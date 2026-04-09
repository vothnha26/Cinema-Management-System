package com.example.cinema.service.showtime;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getAllShowtimes(LocalDate date, Long branchId);
    List<ShowtimeResponse> getShowtimesByBranch(Long branchId, LocalDate date);
    List<ShowtimeResponse> getShowtimesByMovie(Long movieId);
    List<java.time.LocalDate> getDistinctShowtimeDates(Long movieId, Long branchId);
    ShowtimeResponse getShowtimeById(Long id);
    List<SeatResponse> getSeatStatusForShowtime(Long showtimeId, String username);
    ShowtimeResponse createShowtime(ShowtimeRequest request);
    ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request);
    void deleteShowtime(Long id);
}
