package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.MovieDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieDirectorRepository extends JpaRepository<MovieDirector, MovieDirector.MovieDirectorId> {
    List<MovieDirector> findByMovieId(Long movieId);
    void deleteByMovieId(Long movieId);
}
