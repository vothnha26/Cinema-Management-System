package com.example.cinema.repository.showtime;

import com.example.cinema.model.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findAllByRoomIdAndStatusNot(Long roomId, com.example.cinema.model.enums.ShowtimeStatus status);
    
    @Query("SELECT s FROM Showtime s WHERE s.startTime BETWEEN :start AND :end " +
           "AND (:branchId IS NULL OR s.room.branch.id = :branchId) " +
           "AND s.startTime >= CURRENT_TIMESTAMP")
    List<Showtime> findAllByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId);

    @Query("SELECT s FROM Showtime s WHERE s.room.branch.id = :branchId AND s.startTime >= CURRENT_TIMESTAMP")
    List<Showtime> findByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT s FROM Showtime s WHERE s.room.branch.id = :branchId AND s.startTime BETWEEN :start AND :end AND s.startTime >= CURRENT_TIMESTAMP")
    List<Showtime> findByBranchIdAndStartTimeBetween(@Param("branchId") Long branchId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT CAST(s.startTime AS LocalDate) FROM Showtime s " +
           "WHERE (:movieId IS NULL OR s.movie.id = :movieId) " +
           "AND (:branchId IS NULL OR s.room.branch.id = :branchId) " +
           "AND s.startTime >= CURRENT_TIMESTAMP " +
           "ORDER BY CAST(s.startTime AS LocalDate) ASC")
    List<java.time.LocalDate> findDistinctDates(@Param("movieId") Long movieId, @Param("branchId") Long branchId);

    @Query("SELECT DISTINCT s.movie.id FROM Showtime s WHERE s.startTime >= CURRENT_TIMESTAMP AND s.room.branch.id = :branchId")
    List<Long> findMovieIdsWithFutureShowtimes(@Param("branchId") Long branchId);

    @Query("SELECT DISTINCT s.movie.id FROM Showtime s WHERE s.startTime >= CURRENT_TIMESTAMP")
    List<Long> findAllMovieIdsWithFutureShowtimes();

    boolean existsByRoomId(Long roomId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByStartTimeBetweenAndSoldSeats(LocalDateTime start, LocalDateTime end, int soldSeats);
}
