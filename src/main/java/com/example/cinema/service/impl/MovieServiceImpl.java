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
    private final com.example.cinema.repository.ActorRepository actorRepository;
    private final com.example.cinema.repository.DirectorRepository directorRepository;
    private final com.example.cinema.repository.MovieActorRepository movieActorRepository;
    private final com.example.cinema.repository.MovieDirectorRepository movieDirectorRepository;
    private final com.example.cinema.service.MovieMediaService movieMediaService;
    private final ModelMapper modelMapper;
    private final jakarta.persistence.EntityManager entityManager;

    public MovieServiceImpl(MovieRepository movieRepository, 
                            GenreRepository genreRepository,
                            com.example.cinema.repository.ActorRepository actorRepository,
                            com.example.cinema.repository.DirectorRepository directorRepository,
                            com.example.cinema.repository.MovieActorRepository movieActorRepository,
                            com.example.cinema.repository.MovieDirectorRepository movieDirectorRepository,
                            com.example.cinema.service.MovieMediaService movieMediaService, 
                            ModelMapper modelMapper,
                            jakarta.persistence.EntityManager entityManager) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.movieActorRepository = movieActorRepository;
        this.movieDirectorRepository = movieDirectorRepository;
        this.movieMediaService = movieMediaService;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movie -> modelMapper.map(movie, MovieResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public MovieResponse getMovieById(Long id) {
        // Clear cache để đảm bảo load đầy đủ quan hệ mới nhất
        entityManager.flush();
        entityManager.clear();
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim với ID: " + id));
        return modelMapper.map(movie, MovieResponse.class);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest request, MultipartFile poster) {
        Movie movie = modelMapper.map(request, Movie.class);
        
        updateMovieGenres(movie, request);
        uploadAndSetPoster(movie, poster);

        Movie savedMovie = movieRepository.save(movie);
        
        // Lưu Actor & Director sau khi có movie ID
        updateMovieActorsAndDirectors(savedMovie, request);
        
        // Refresh movie object to include new relations for mapping
        return getMovieById(savedMovie.getId());
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile poster) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim với ID: " + id));

        modelMapper.map(request, movie);
        
        updateMovieGenres(movie, request);
        uploadAndSetPoster(movie, poster);

        Movie updatedMovie = movieRepository.save(movie);
        
        // Cập nhật lại danh sách Actor/Director
        movieActorRepository.deleteByMovieId(id);
        movieDirectorRepository.deleteByMovieId(id);
        updateMovieActorsAndDirectors(updatedMovie, request);

        return getMovieById(id);
    }

    private void updateMovieGenres(Movie movie, MovieRequest request) {
        if (request.getGenreIds() != null) {
            List<com.example.cinema.model.entity.Genre> genres = genreRepository.findAllById(request.getGenreIds());
            movie.setGenres(new HashSet<>(genres));
        }
    }

    private void updateMovieActorsAndDirectors(Movie movie, MovieRequest request) {
        if (request.getActorIds() != null) {
            List<com.example.cinema.model.entity.MovieActor> movieActors = request.getActorIds().stream().map(actorId -> {
                com.example.cinema.model.entity.Actor actor = actorRepository.findById(actorId)
                        .orElseThrow(() -> new AppException("Không tìm thấy diễn viên ID: " + actorId));
                com.example.cinema.model.entity.MovieActor movieActor = new com.example.cinema.model.entity.MovieActor();
                movieActor.setId(new com.example.cinema.model.entity.MovieActor.MovieActorId(movie.getId(), actorId));
                movieActor.setMovie(movie);
                movieActor.setActor(actor);
                return movieActor;
            }).collect(Collectors.toList());
            movieActorRepository.saveAll(movieActors);
        }

        if (request.getDirectorIds() != null) {
            List<com.example.cinema.model.entity.MovieDirector> movieDirectors = request.getDirectorIds().stream().map(directorId -> {
                com.example.cinema.model.entity.Director director = directorRepository.findById(directorId)
                        .orElseThrow(() -> new AppException("Không tìm thấy đạo diễn ID: " + directorId));
                com.example.cinema.model.entity.MovieDirector movieDirector = new com.example.cinema.model.entity.MovieDirector();
                movieDirector.setId(new com.example.cinema.model.entity.MovieDirector.MovieDirectorId(movie.getId(), directorId));
                movieDirector.setMovie(movie);
                movieDirector.setDirector(director);
                movieDirector.setRole(com.example.cinema.model.enums.DirectorRole.MAIN);
                return movieDirector;
            }).collect(Collectors.toList());
            movieDirectorRepository.saveAll(movieDirectors);
        }
    }

    private void uploadAndSetPoster(Movie movie, MultipartFile poster) {
        if (poster != null && !poster.isEmpty()) {
            try {
                String posterUrl = movieMediaService.uploadPoster(poster);
                movie.setPosterUrl(posterUrl);
            } catch (IOException e) {
                throw new AppException("Lỗi khi tải ảnh lên Cloudinary");
            }
        }
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new AppException("Không tìm thấy phim với ID: " + id);
        }
        movieActorRepository.deleteByMovieId(id);
        movieDirectorRepository.deleteByMovieId(id);
        movieRepository.deleteById(id);
    }
}
