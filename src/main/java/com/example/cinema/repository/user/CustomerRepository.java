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

    @Query("SELECT COUNT(DISTINCT c) FROM Customer c JOIN Booking b ON c.id = b.customer.id " +
           "WHERE b.createdAt BETWEEN :start AND :end " +
           "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId)")
    long countNewCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId);

    @Query("SELECT c.membershipLevel.name, COUNT(c) FROM Customer c GROUP BY c.membershipLevel.name")
    List<Object[]> countByMembershipTier();

    @Query("SELECT c FROM Customer c JOIN Booking b ON c.id = b.customer.id " +
           "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.createdAt BETWEEN :start AND :end " +
           "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId) " +
           "GROUP BY c ORDER BY SUM(b.totalPrice) DESC")
    List<Customer> findTopCustomersBySpend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("branchId") Long branchId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.customer.id = :customerId AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId)")
    BigDecimal calculateTotalSpendByCustomer(@Param("customerId") Long customerId, @Param("branchId") Long branchId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.id = :customerId AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND (:branchId IS NULL OR b.showtime.room.branch.id = :branchId)")
    long countVisitsByCustomer(@Param("customerId") Long customerId, @Param("branchId") Long branchId);
}
