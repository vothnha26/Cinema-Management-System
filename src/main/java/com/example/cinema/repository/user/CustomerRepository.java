package com.example.cinema.repository.user;

import com.example.cinema.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByUserUsername(String username);
    Optional<Customer> findByPhone(String phone);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :start AND :end")
    long countNewCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.membershipTier, COUNT(c) FROM Customer c GROUP BY c.membershipTier")
    List<Object[]> countByMembershipTier();

    @Query("SELECT c FROM Customer c JOIN Booking b ON c.id = b.customer.id " +
           "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY c.id ORDER BY SUM(b.totalPrice) DESC")
    List<Customer> findTopCustomersBySpend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.customer.id = :customerId AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    BigDecimal calculateTotalSpendByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.id = :customerId AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countVisitsByCustomer(@Param("customerId") Long customerId);
}
