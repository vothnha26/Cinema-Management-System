package com.example.cinema.repository;

import com.example.cinema.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCode(String code);
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
    
    List<Promotion> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
        LocalDate startDate, LocalDate endDate
    );

    List<Promotion> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDate startDate, LocalDate endDate
    );
}
