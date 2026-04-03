package com.example.cinema.repository.booking;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Booking> findByStatusAndShowtimeStartTimeBetween(BookingStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'CONFIRMED' OR b.status = 'CHECKED_IN'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(bd) FROM BookingDetail bd WHERE bd.booking.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countTotalTickets();

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE (b.status = 'CONFIRMED' OR b.status = 'CHECKED_IN') AND b.createdAt >= :date")
    BigDecimal calculateRevenueByDate(@Param("date") LocalDateTime date);
}
