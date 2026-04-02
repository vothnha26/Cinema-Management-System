package com.example.cinema.controller;

import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    
    private final MovieService movieService;
    
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }
    
    @GetMapping("/showing")
    public ResponseEntity<List<MovieResponse>> getShowingMovies() {
        return ResponseEntity.ok(movieService.getShowingMovies());
    }
    
    @GetMapping("/coming")
    public ResponseEntity<List<MovieResponse>> getComingSoonMovies() {
        return ResponseEntity.ok(movieService.getComingSoonMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@RequestBody com.example.cinema.model.dto.request.MovieRequest request) {
        System.out.println(">>> Backend: Nhận yêu cầu tạo phim: " + request.getTitle());
        System.out.println(">>> Dữ liệu Trailer: " + request.getTrailerUrl());
        System.out.println(">>> Dữ liệu Diễn viên: " + request.getActors());
        try {
            MovieResponse response = movieService.createMovie(request);
            System.out.println(">>> Backend: Lưu phim thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(">>> Backend Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
