package com.example.cinema.repository;

import com.example.cinema.model.entity.MovieDirector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieDirectorRepository extends JpaRepository<MovieDirector, MovieDirector.MovieDirectorId> {
    java.util.List<MovieDirector> findByMovieId(Long movieId);
}
