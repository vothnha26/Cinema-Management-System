package com.example.cinema.service;

import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;
import com.example.cinema.model.dto.tmdb.TMDBMovieDto;

public interface TMDBService {
    TMDBSearchResponse searchMovies(String query);
    TMDBMovieDto getMovieDetails(Long tmdbId);
}
