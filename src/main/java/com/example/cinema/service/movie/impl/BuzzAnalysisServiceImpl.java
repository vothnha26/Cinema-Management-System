package com.example.cinema.service.movie.impl;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.service.movie.BuzzAnalysisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BuzzAnalysisServiceImpl implements BuzzAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(BuzzAnalysisServiceImpl.class);

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;

    // Cache đơn giản: Map<MovieId, BuzzScoreInfo>
    private final Map<Long, BuzzScoreInfo> buzzCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_HOURS = 6;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    public BuzzAnalysisServiceImpl(MovieRepository movieRepository, RestTemplate restTemplate) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<Long, Double> getExternalBuzzScores() {
        // Chỉ lấy điểm Buzz cho các phim đang hoạt động để tránh gọi API lãng phí
        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.SHOWING || 
                            m.getStatus() == MovieStatus.NOW_SHOWING || 
                            m.getStatus() == MovieStatus.PRE_RELEASE)
                .collect(Collectors.toList());
        
        Map<Long, Double> scores = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Movie movie : activeMovies) {
            BuzzScoreInfo cached = buzzCache.get(movie.getId());
            
            if (cached != null && cached.timestamp.isAfter(now.minusHours(CACHE_EXPIRY_HOURS))) {
                scores.put(movie.getId(), cached.score);
            } else {
                double score = fetchBuzzFromTMDB(movie);
                buzzCache.put(movie.getId(), new BuzzScoreInfo(score, now));
                scores.put(movie.getId(), score);
            }
        }
        return scores;
    }

    private double fetchBuzzFromTMDB(Movie movie) {
        try {
            // Ưu tiên tìm kiếm bằng TMDB ID nếu có để chính xác tuyệt đối
            String url;
            if (movie.getTmdbId() != null) {
                url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movie.getTmdbId())
                        .queryParam("api_key", apiKey)
                        .toUriString();
            } else {
                url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", movie.getTitle())
                        .toUriString();
            }

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                Object popularity = null;
                if (movie.getTmdbId() != null) {
                    popularity = response.get("popularity");
                } else if (response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    if (!results.isEmpty()) {
                        popularity = results.get(0).get("popularity");
                    }
                }

                if (popularity instanceof Number) {
                    double popValue = ((Number) popularity).doubleValue();
                    double buzz = Math.min(100.0, Math.log10(popValue + 1) * 25);
                    return Math.max(40.0, buzz);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi gọi TMDB API cho phim {}: ", movie.getTitle(), e);
        }

        return 50.0 + (Math.random() * 10);
    }

    private static class BuzzScoreInfo {
        double score;
        LocalDateTime timestamp;

        BuzzScoreInfo(double score, LocalDateTime timestamp) {
            this.score = score;
            this.timestamp = timestamp;
        }
    }
}
