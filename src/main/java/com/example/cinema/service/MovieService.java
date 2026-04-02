package com.example.cinema.service;

import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import java.util.List;

public interface MovieService {
    List<MovieResponse> getAllMovies();
    List<MovieResponse> getShowingMovies();
    List<MovieResponse> getComingSoonMovies();
    MovieResponse getMovieById(Long id);
    MovieResponse createMovie(MovieRequest request);
}
