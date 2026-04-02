package com.example.cinema.repository;

import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface SeatPriceRepository extends JpaRepository<SeatPrice, Long> {
    
    @Query("SELECT sp FROM SeatPrice sp WHERE sp.roomType = :roomType " +
           "AND sp.seatType = :seatType AND sp.isActive = true " +
           "AND sp.effectiveDate <= :date ORDER BY sp.effectiveDate DESC LIMIT 1")
    Optional<SeatPrice> findLatestPrice(@Param("roomType") RoomType roomType, 
                                       @Param("seatType") SeatType seatType, 
                                       @Param("date") LocalDate date);
}
