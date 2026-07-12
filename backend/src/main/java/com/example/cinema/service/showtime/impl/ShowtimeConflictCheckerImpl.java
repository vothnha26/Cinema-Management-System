package com.example.cinema.service.showtime.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.service.showtime.IShowtimeConflictChecker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShowtimeConflictCheckerImpl implements IShowtimeConflictChecker {

    @Override
    public void validate(Showtime newShowtime, List<Showtime> existingShowtimes) {
        Showtime conflict = findConflict(newShowtime, existingShowtimes);
        if (conflict != null) {
            throw new AppException(String.format("Xung đột lịch chiếu: Phòng %s đã có suất chiếu từ %s đến %s (%s)",
                    newShowtime.getRoom().getName(),
                    conflict.getStartTime().toLocalTime(),
                    conflict.getEndTime().toLocalTime(),
                    conflict.getMovie().getTitle()));
        }
    }

    @Override
    public Showtime findConflict(Showtime newShowtime, List<Showtime> existingShowtimes) {
        LocalDateTime start = newShowtime.getStartTime();
        LocalDateTime end = newShowtime.getEndTime();
        
        for (Showtime s : existingShowtimes) {
            if (start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime())) {
                return s;
            }
        }
        return null;
    }
}
