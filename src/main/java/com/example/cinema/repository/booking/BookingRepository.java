package com.example.cinema.repository.booking;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(bd) FROM BookingDetail bd WHERE bd.booking.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countTotalTickets();

    @Query("SELECT FUNCTION('DATE', b.createdAt) as date, SUM(b.totalPrice) as revenue " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', b.createdAt)")
    List<Object[]> calculateRevenueByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b.showtime.movie.title, SUM(b.totalPrice) " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY b.showtime.movie.title")
    List<Object[]> calculateRevenueByMovie(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('HOUR', b.createdAt) as hour, SUM(b.totalPrice) as revenue " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('HOUR', b.createdAt)")
    List<Object[]> calculateRevenueByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b.showtime.room.name, SUM(b.totalPrice) " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY b.showtime.room.name")
    List<Object[]> calculateRevenueByRoom(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
