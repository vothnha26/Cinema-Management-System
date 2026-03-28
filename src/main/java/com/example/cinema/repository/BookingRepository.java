package com.example.cinema.repository;

import com.example.cinema.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')")
    Long countTotalTickets();

    @Query("SELECT DATE(b.createdAt) as date, SUM(b.totalPrice) as amount " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND b.createdAt >= :startDate " +
           "GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt) ASC")
    List<Object[]> calculateRevenueByDate(@Param("startDate") LocalDateTime startDate);
}
