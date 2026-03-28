package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComboRepository extends JpaRepository<Combo, Long> {
    List<Combo> findByIsActiveTrue();
}
