package com.example.cinema.service.movie;

import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MovieService {
    List<MovieResponse> getAllMovies();
    MovieResponse getMovieById(Long id);
    MovieResponse createMovie(MovieRequest request, MultipartFile poster);
    MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile poster);
    void deleteMovie(Long id);
}
