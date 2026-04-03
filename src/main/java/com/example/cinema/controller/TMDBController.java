package com.example.cinema.controller;

import com.example.cinema.service.movie.tmdb.TMDBService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tmdb")
public class TMDBController {

    private final TMDBService tmdbService;

    public TMDBController(TMDBService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query) {
        System.out.println(">>> Controller: Nhận yêu cầu tìm phim: " + query);
        try {
            return ResponseEntity.ok(tmdbService.searchMovies(query));
        } catch (Exception e) {
            System.err.println(">>> Controller Search Error: " + e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/movie/{tmdbId}")
    public ResponseEntity<?> getMovieDetails(@PathVariable Long tmdbId) {
        System.out.println(">>> Controller: Nhận yêu cầu lấy chi tiết phim ID: " + tmdbId);
        try {
            return ResponseEntity.ok(tmdbService.getMovieDetails(tmdbId));
        } catch (Exception e) {
            System.err.println(">>> Controller Detail Error: " + e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
