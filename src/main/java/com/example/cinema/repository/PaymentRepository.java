package com.example.cinema.repository;

import com.example.cinema.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingCustomerIdOrderByPaidAtDesc(Long customerId);
    Optional<Payment> findByBookingBookingCode(String bookingCode);
}
