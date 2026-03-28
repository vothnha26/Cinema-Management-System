package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.repository.GenreRepository;
import com.example.cinema.repository.MovieRepository;
import com.example.cinema.service.CloudinaryService;
import com.example.cinema.service.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    public MovieServiceImpl(MovieRepository movieRepository, 
                            GenreRepository genreRepository,
                            CloudinaryService cloudinaryService, 
                            ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.cloudinaryService = cloudinaryService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movie -> modelMapper.map(movie, MovieResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim với ID: " + id));
        return modelMapper.map(movie, MovieResponse.class);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest request, MultipartFile poster) {
        Movie movie = modelMapper.map(request, Movie.class);
        
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
            movie.setGenres(new HashSet<>(genres));
        }

        if (poster != null && !poster.isEmpty()) {
            try {
                Map uploadResult = cloudinaryService.upload(poster, "movies");
                movie.setPosterUrl((String) uploadResult.get("secure_url"));
            } catch (IOException e) {
                throw new AppException("Lỗi khi tải ảnh lên Cloudinary");
            }
        }

        Movie savedMovie = movieRepository.save(movie);
        return modelMapper.map(savedMovie, MovieResponse.class);
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile poster) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim với ID: " + id));

        modelMapper.map(request, movie);

        if (request.getGenreIds() != null) {
            List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
            movie.setGenres(new HashSet<>(genres));
        }

        if (poster != null && !poster.isEmpty()) {
            try {
                Map uploadResult = cloudinaryService.upload(poster, "movies");
                movie.setPosterUrl((String) uploadResult.get("secure_url"));
            } catch (IOException e) {
                throw new AppException("Lỗi khi tải ảnh lên Cloudinary");
            }
        }

        Movie updatedMovie = movieRepository.save(movie);
        return modelMapper.map(updatedMovie, MovieResponse.class);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new AppException("Không tìm thấy phim với ID: " + id);
        }
        movieRepository.deleteById(id);
    }
}
