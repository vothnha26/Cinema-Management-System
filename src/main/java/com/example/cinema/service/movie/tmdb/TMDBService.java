package com.example.cinema.service.movie.tmdb;

import com.example.cinema.model.dto.tmdb.TMDBMovieDto;
import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;

public interface TMDBService {
    TMDBSearchResponse searchMovies(String query);

    TMDBMovieDto getMovieDetails(Long tmdbId);
}
