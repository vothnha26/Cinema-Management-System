package com.example.cinema.repository;

import com.example.cinema.model.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);
    void deleteByRoomId(Long roomId);
    java.util.Optional<Seat> findByRoomIdAndRowCharAndColNum(Long roomId, String rowChar, Integer colNum);
}
