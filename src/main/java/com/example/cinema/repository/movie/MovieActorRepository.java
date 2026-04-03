package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieActorRepository extends JpaRepository<MovieActor, MovieActor.MovieActorId> {
    List<MovieActor> findByMovieId(Long movieId);
    void deleteByMovieId(Long movieId);
}
