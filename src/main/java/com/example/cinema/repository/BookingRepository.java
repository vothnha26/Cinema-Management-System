package com.example.cinema.repository;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Booking> findByStatusAndShowtimeStartTimeBetween(BookingStatus status, LocalDateTime start, LocalDateTime end);
}
