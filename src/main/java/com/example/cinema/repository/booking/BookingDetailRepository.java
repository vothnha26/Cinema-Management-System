package com.example.cinema.repository.booking;

import com.example.cinema.model.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    
    @Query("SELECT bd.seat.id FROM BookingDetail bd WHERE bd.booking.showtime.id = :showtimeId " +
           "AND bd.booking.status != 'CANCELLED'")
    List<Long> findBookedSeatIdsByShowtime(@Param("showtimeId") Long showtimeId);

    @Modifying
    @Transactional
    @Query("UPDATE BookingDetail bd SET bd.seat = null WHERE bd.seat.id IN (SELECT s.id FROM Seat s WHERE s.room.id = :roomId)")
    void updateSeatToNullByRoomId(@Param("roomId") Long roomId);
}
