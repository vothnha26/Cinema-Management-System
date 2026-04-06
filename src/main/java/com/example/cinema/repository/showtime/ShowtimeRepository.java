package com.example.cinema.repository.showtime;

import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    boolean existsByRoomId(Long roomId);
    List<Showtime> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Showtime> findByMovieId(Long movieId);
    void deleteByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    void deleteByStartTimeBetweenAndIdNotIn(LocalDateTime start, LocalDateTime end, List<Long> ids);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Showtime s WHERE s.startTime >= :start AND s.startTime <= :end AND s.soldSeats = :soldSeats")
    void deleteByStartTimeBetweenAndSoldSeats(LocalDateTime start, LocalDateTime end, Integer soldSeats);

    List<Showtime> findByMovieIdAndStatus(Long movieId, ShowtimeStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Showtime> findOverlappingShowtimes(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
}
