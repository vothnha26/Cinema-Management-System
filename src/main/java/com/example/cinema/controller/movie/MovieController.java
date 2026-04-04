package com.example.cinema.controller.movie;

import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.service.movie.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        List<MovieResponse> movies = movieService.getAllMovies();
        return ResponseEntity.ok(ApiResponse.ok(movies));
    }

    @GetMapping("/showing")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getShowingMovies() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getShowingMovies()));
    }

    @GetMapping("/coming")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getComingSoonMovies() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getComingSoonMovies()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.ok(movie));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(
            @RequestPart("movie") @Valid MovieRequest request,
            @RequestPart(value = "poster", required = false) MultipartFile poster) {
        MovieResponse movie = movieService.createMovie(request, poster);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(movie));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable Long id,
            @RequestPart("movie") @Valid MovieRequest request,
            @RequestPart(value = "poster", required = false) MultipartFile poster) {
        MovieResponse movie = movieService.updateMovie(id, request, poster);
        return ResponseEntity.ok(ApiResponse.ok(movie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
