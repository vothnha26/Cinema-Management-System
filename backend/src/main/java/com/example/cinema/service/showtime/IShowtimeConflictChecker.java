package com.example.cinema.service.showtime;

import com.example.cinema.model.entity.Showtime;
import java.util.List;

public interface IShowtimeConflictChecker {
    void validate(Showtime newShowtime, List<Showtime> existingShowtimes);
    Showtime findConflict(Showtime newShowtime, List<Showtime> existingShowtimes);
}
