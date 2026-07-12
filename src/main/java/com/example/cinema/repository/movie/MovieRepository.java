package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(MovieStatus status);
    List<Movie> findAllByStatusIn(List<MovieStatus> statuses);
    Optional<Movie> findByTitle(String title);
}
