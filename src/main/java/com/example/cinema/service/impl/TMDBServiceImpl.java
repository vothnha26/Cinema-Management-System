package com.example.cinema.service.impl;

import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;
import com.example.cinema.model.dto.tmdb.TMDBMovieDto;
import com.example.cinema.service.TMDBService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TMDBServiceImpl implements TMDBService {

    private final RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.image-base}")
    private String imageBase;

    public TMDBServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        if (apiKey != null) {
            apiKey = apiKey.trim();
            System.out.println(">>> TMDB Service initialized with key: " + 
                (apiKey.length() > 5 ? apiKey.substring(0, 5) + "..." : "EMPTY"));
        }
    }

    @Override
    public TMDBSearchResponse searchMovies(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("language", "vi")
                    .build()
                    .encode()
                    .toUriString();

            System.out.println(">>> Calling TMDB (Search): " + url.replace(apiKey, "HIDDEN_KEY"));
            TMDBSearchResponse response = restTemplate.getForObject(url, TMDBSearchResponse.class);
            if (response != null && response.getResults() != null) {
                response.getResults().forEach(this::processPosters);
            }
            return response;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "Lỗi từ TMDB: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            System.err.println(">>> " + errorMsg);
            throw new org.springframework.web.server.ResponseStatusException(e.getStatusCode(), errorMsg);
        } catch (Exception e) {
            System.err.println(">>> TMDB Unexpected Error: " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi kết nối TMDB: " + e.getMessage());
        }
    }

    @Override
    public TMDBMovieDto getMovieDetails(Long tmdbId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "vi")
                    .queryParam("append_to_response", "credits,videos")
                    .build()
                    .encode()
                    .toUriString();

            System.out.println(">>> Calling TMDB (Full Detail): " + url.replace(apiKey, "HIDDEN_KEY"));
            TMDBMovieDto movie = restTemplate.getForObject(url, TMDBMovieDto.class);
            if (movie != null) {
                processPosters(movie);
            }
            return movie;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "Lỗi chi tiết từ TMDB: " + e.getStatusCode();
            throw new org.springframework.web.server.ResponseStatusException(e.getStatusCode(), errorMsg);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi lấy chi tiết TMDB: " + e.getMessage());
        }
    }

    private void processPosters(TMDBMovieDto movie) {
        if (movie.getPosterPath() != null) {
            movie.setPosterPath(imageBase + movie.getPosterPath());
        }
        if (movie.getBackdropPath() != null) {
            movie.setBackdropPath(imageBase + movie.getBackdropPath());
        }
    }
}
