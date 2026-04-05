package com.example.cinema.repository.user;

import com.example.cinema.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByUserUsername(String username);
    Optional<Customer> findByPhone(String phone);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :start AND :end")
    long countNewCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
