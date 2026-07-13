package com.example.cinema.repository.room;

import com.example.cinema.model.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);
    Optional<Seat> findByRoomIdAndRowCharAndColNum(Long roomId, String rowChar, Integer colNum);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Seat s WHERE s.room.id = :roomId")
    void deleteByRoomId(@org.springframework.data.repository.query.Param("roomId") Long roomId);
    
    List<Seat> findByRoomIdAndStatus(Long roomId, com.example.cinema.model.enums.SeatStatus status);
}
