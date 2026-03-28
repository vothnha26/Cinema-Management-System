package com.example.cinema.service.movie.impl;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.service.movie.BuzzAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuzzAnalysisServiceImpl implements BuzzAnalysisService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;

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
        List<Movie> movies = movieRepository.findAll();
        Map<Long, Double> scores = new HashMap<>();

        for (Movie movie : movies) {
            double score = fetchBuzzFromTMDB(movie.getTitle());
            scores.put(movie.getId(), score);
        }
        return scores;
    }

    private double fetchBuzzFromTMDB(String title) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", title)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    // Lấy kết quả đầu tiên (thường là phim khớp nhất)
                    Map<String, Object> firstResult = results.get(0);
                    Object popularity = firstResult.get("popularity");
                    
                    if (popularity instanceof Number) {
                        double popValue = ((Number) popularity).doubleValue();
                        // TMDB Popularity có thể rất cao (hàng ngàn), chúng ta chuẩn hóa về 0-100
                        // Sử dụng log để nén các phim cực hot lại, đảm bảo thang điểm 0-100
                        double buzz = Math.min(100.0, Math.log10(popValue + 1) * 25);
                        return Math.max(40.0, buzz);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi gọi TMDB API cho phim " + title + ": " + e.getMessage());
        }
        
        // Fallback: Nếu lỗi API, trả về điểm mặc định
        return 50.0 + (Math.random() * 10);
    }
}
