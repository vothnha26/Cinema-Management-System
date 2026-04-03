package com.example.cinema.repository.room;

import com.example.cinema.model.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);
    Optional<Seat> findByRoomIdAndRowCharAndColNum(Long roomId, String rowChar, Integer colNum);
    void deleteByRoomId(Long roomId);
    List<Seat> findByRoomIdAndStatusTrue(Long roomId);
}
