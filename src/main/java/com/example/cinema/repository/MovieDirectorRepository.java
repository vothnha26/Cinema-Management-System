package com.example.cinema.repository;

import com.example.cinema.model.entity.MovieDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieDirectorRepository extends JpaRepository<MovieDirector, MovieDirector.MovieDirectorId> {
    void deleteByMovieId(Long movieId);
}
