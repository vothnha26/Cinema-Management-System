package com.example.cinema.repository;

import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    
    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room WHERE s.status = :status")
    List<Showtime> findByStatusWithMovieAndRoom(@Param("status") ShowtimeStatus status);

    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId " +
           "AND s.startTime >= :startOfDay AND s.startTime <= :endOfDay " +
           "AND s.status != 'CANCELLED'")
    List<Showtime> findByRoomAndDate(
            @Param("roomId") Long roomId, 
            @Param("startOfDay") LocalDateTime startOfDay, 
            @Param("endOfDay") LocalDateTime endOfDay);
    
    List<Showtime> findByMovieIdAndStatus(Long movieId, ShowtimeStatus status);
}
