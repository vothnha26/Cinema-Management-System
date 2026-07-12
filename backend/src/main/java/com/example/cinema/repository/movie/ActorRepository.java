package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByName(String name);
    List<Actor> findByNameContainingIgnoreCase(String name);
}
