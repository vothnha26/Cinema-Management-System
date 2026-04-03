package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
    boolean existsByCode(String code);
    Optional<Promotion> findByCode(String code);
}
