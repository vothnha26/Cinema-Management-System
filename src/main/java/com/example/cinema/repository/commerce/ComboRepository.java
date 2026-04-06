package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface ComboRepository extends JpaRepository<Combo, Long> {
    List<Combo> findByIsActiveTrue();

    @Query("SELECT bc.combo.name, SUM(bc.quantity), SUM(bc.price * bc.quantity) FROM BookingCombo bc " +
           "WHERE bc.booking.status IN ('CONFIRMED', 'CHECKED_IN') AND bc.booking.createdAt BETWEEN :start AND :end " +
           "GROUP BY bc.combo.name ORDER BY SUM(bc.quantity) DESC")
    List<Object[]> findTopSellingCombos(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);
}
