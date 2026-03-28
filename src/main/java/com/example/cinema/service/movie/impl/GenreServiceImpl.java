package com.example.cinema.service.movie.impl;

import com.example.cinema.model.entity.Genre;
import com.example.cinema.repository.movie.GenreRepository;
import com.example.cinema.service.movie.GenreService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }
}
