package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
    boolean existsByCode(String code);
    Optional<Promotion> findByCode(String code);

    @Query("SELECT p.name, p.startDate, p.endDate, COUNT(b), SUM(b.totalPrice) FROM Promotion p " +
           "LEFT JOIN Booking b ON p.id = b.promotion.id " +
           "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND b.createdAt BETWEEN :start AND :end " +
           "GROUP BY p.name, p.startDate, p.endDate")
    List<Object[]> calculatePromotionStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
