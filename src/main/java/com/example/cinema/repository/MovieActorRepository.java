package com.example.cinema.repository;

import com.example.cinema.model.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieActorRepository extends JpaRepository<MovieActor, MovieActor.MovieActorId> {
    java.util.List<MovieActor> findByMovieId(Long movieId);
}
