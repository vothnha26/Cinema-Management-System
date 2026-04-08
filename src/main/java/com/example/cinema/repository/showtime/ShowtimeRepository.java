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
    
    @Query("SELECT s FROM Showtime s WHERE s.startTime BETWEEN :start AND :end " +
           "AND (:branchId IS NULL OR s.room.branch.id = :branchId)")
    List<Showtime> findAllByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId);

    @Query("SELECT s FROM Showtime s WHERE s.room.branch.id = :branchId")
    List<Showtime> findByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT s FROM Showtime s WHERE s.room.branch.id = :branchId AND s.startTime BETWEEN :start AND :end")
    List<Showtime> findByBranchIdAndStartTimeBetween(@Param("branchId") Long branchId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    boolean existsByRoomId(Long roomId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByStartTimeBetweenAndSoldSeats(LocalDateTime start, LocalDateTime end, int soldSeats);
}
