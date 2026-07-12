package com.example.cinema.repository.room;

import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatPriceRepository extends JpaRepository<SeatPrice, Long> {
    List<SeatPrice> findAllByRoomTypeAndIsActiveTrue(RoomType roomType);

    List<SeatPrice> findByRoomTypeAndSeatTypeAndIsActiveTrue(RoomType roomType, SeatType seatType);

    List<SeatPrice> findByRoomTypeAndSeatType(RoomType roomType, SeatType seatType);

    List<SeatPrice> findAllByIsActiveTrue();
}
