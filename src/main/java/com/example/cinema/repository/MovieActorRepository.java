package com.example.cinema.repository;

import com.example.cinema.model.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieActorRepository extends JpaRepository<MovieActor, MovieActor.MovieActorId> {
    void deleteByMovieId(Long movieId);
}
