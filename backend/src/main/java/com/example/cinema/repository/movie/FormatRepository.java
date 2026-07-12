package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FormatRepository extends JpaRepository<Format, Long> {
    Optional<Format> findByName(String name);
}
