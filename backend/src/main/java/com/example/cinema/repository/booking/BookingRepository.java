package com.example.cinema.repository.booking;

import com.example.cinema.model.entity.Booking;
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
       List<Booking> findAllByStatusAndCreatedAtBefore(com.example.cinema.model.enums.BookingStatus status, java.time.LocalDateTime dateTime);

       @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.createdAt BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId)")
       BigDecimal calculateTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId);

       @Query("SELECT COUNT(bd) FROM BookingDetail bd WHERE bd.booking.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND bd.booking.createdAt BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR bd.booking.showtime.room.branch.id = :branchId)")
       long countTotalTickets(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId);

       @Query(value = "SELECT DATE(b.created_at) as dt, SUM(b.total_price) as revenue " +
                     "FROM bookings b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR EXISTS (SELECT 1 FROM showtimes s JOIN rooms r ON s.room_id = r.id WHERE s.id = b.showtime_id AND r.branch_id = :branchId)) " +
                     "GROUP BY dt", nativeQuery = true)
       List<Object[]> calculateRevenueByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT m.title, SUM(b.total_price) " +
                     "FROM bookings b " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY m.title", nativeQuery = true)
       List<Object[]> calculateRevenueByMovie(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT HOUR(b.created_at) as hr, SUM(b.total_price) as revenue " +
                     "FROM bookings b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR EXISTS (SELECT 1 FROM showtimes s JOIN rooms r ON s.room_id = r.id WHERE s.id = b.showtime_id AND r.branch_id = :branchId)) " +
                     "GROUP BY hr", nativeQuery = true)
       List<Object[]> calculateRevenueByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT r.name, SUM(b.total_price) " +
                     "FROM bookings b " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY r.name", nativeQuery = true)
       List<Object[]> calculateRevenueByRoom(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT SUM(bc.price * bc.quantity) FROM booking_combos bc " +
                     "JOIN bookings b ON bc.booking_id = b.id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId)", nativeQuery = true)
       BigDecimal calculateComboRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT COUNT(*) FROM bookings b " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId)", nativeQuery = true)
       long countOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT COUNT(*) FROM booking_combos bc " +
                     "JOIN bookings b ON bc.booking_id = b.id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId)", nativeQuery = true)
       long countFnBOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT st.name, COUNT(bd.id), SUM(bd.price) FROM booking_details bd " +
                     "JOIN bookings b ON bd.booking_id = b.id " +
                     "JOIN seats s_seat ON bd.seat_id = s_seat.id " +
                     "JOIN seat_types st ON s_seat.seat_type_id = st.id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY st.name", nativeQuery = true)
       List<Object[]> calculateStatsBySeatType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT b.status, COUNT(bd.id) FROM booking_details bd " +
                     "JOIN bookings b ON bd.booking_id = b.id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY b.status", nativeQuery = true)
       List<Object[]> countTicketsByStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT p.payment_method, COUNT(b.id) FROM bookings b " +
                     "JOIN payments p ON b.id = p.booking_id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY p.payment_method", nativeQuery = true)
       List<Object[]> countBookingsByChannel(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query(value = "SELECT DATE(b.created_at) as date, " +
                     "SUM(CASE WHEN b.status IN ('CONFIRMED', 'CHECKED_IN') THEN 1 ELSE 0 END) as sold, " +
                     "SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
                     "FROM bookings b " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "WHERE b.created_at BETWEEN :start AND :end " +
                     "AND (:branchId IS NULL OR r.branch_id = :branchId) " +
                     "GROUP BY DATE(b.created_at)", nativeQuery = true)
       List<Object[]> calculateTicketTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                     @Param("branchId") Long branchId);

       @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId) " +
                     "ORDER BY b.createdAt DESC")
       List<Booking> findRecentBookings(@Param("branchId") Long branchId,
                     org.springframework.data.domain.Pageable pageable);

       long countByShowtimeId(Long showtimeId);
}
