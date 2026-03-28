package com.example.cinema.controller;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.service.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Genre>>> getAllGenres() {
        return ResponseEntity.ok(ApiResponse.ok(genreService.getAllGenres()));
    }
}
