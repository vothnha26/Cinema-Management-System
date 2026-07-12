package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByName(String name);
    List<Director> findByNameContainingIgnoreCase(String name);
}
