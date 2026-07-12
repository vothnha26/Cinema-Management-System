package com.example.cinema.service.movie.tmdb.impl;

import com.example.cinema.model.dto.tmdb.TMDBMovieDto;
import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.service.movie.tmdb.TMDBService;
import com.example.cinema.service.movie.tmdb.mapper.TMDBMovieMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TMDBServiceImpl implements TMDBService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TMDBServiceImpl.class);

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final TMDBMovieMapper movieMapper;

    public TMDBServiceImpl(RestTemplate restTemplate, TMDBMovieMapper movieMapper) {
        this.restTemplate = restTemplate;
        this.movieMapper = movieMapper;
    }

    @Override
    public TMDBMovieDto getMovieDetails(Long tmdbId) {
        String url = "https://api.themoviedb.org/3/movie/{tmdbId}?api_key={apiKey}&language=vi-VN&append_to_response=credits,videos";
        try {
            return restTemplate.getForObject(url, TMDBMovieDto.class, tmdbId, apiKey);
        } catch (Exception e) {
            log.error("Error fetching TMDB movie details for ID {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    @Override
    public TMDBSearchResponse searchMovies(String query) {
        String url = "https://api.themoviedb.org/3/search/movie?api_key={apiKey}&query={query}&language=vi-VN";
        try {
            return restTemplate.getForObject(url, TMDBSearchResponse.class, apiKey, query);
        } catch (Exception e) {
            log.error("Error searching TMDB movies with query {}: {}", query, e.getMessage());
            return new TMDBSearchResponse();
        }
    }
}
