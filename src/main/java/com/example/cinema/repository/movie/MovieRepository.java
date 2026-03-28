package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(MovieStatus status);
}
