package com.example.cinema.service.movie.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.dto.response.PersonResponse;
import com.example.cinema.model.entity.Actor;
import com.example.cinema.model.entity.Director;
import com.example.cinema.model.entity.Format;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.MovieActor;
import com.example.cinema.model.entity.MovieDirector;
import com.example.cinema.model.enums.AgeRating;
import com.example.cinema.model.enums.DirectorRole;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.movie.ActorRepository;
import com.example.cinema.repository.movie.DirectorRepository;
import com.example.cinema.repository.movie.FormatRepository;
import com.example.cinema.repository.movie.GenreRepository;
import com.example.cinema.repository.movie.MovieActorRepository;
import com.example.cinema.repository.movie.MovieDirectorRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.service.movie.MovieMediaService;
import com.example.cinema.service.movie.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieDirectorRepository movieDirectorRepository;
    private final FormatRepository formatRepository;
    private final MovieMediaService movieMediaService;
    private final ModelMapper modelMapper;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository,
            GenreRepository genreRepository,
            ActorRepository actorRepository,
            DirectorRepository directorRepository,
            MovieActorRepository movieActorRepository,
            MovieDirectorRepository movieDirectorRepository,
            FormatRepository formatRepository,
            MovieMediaService movieMediaService,
            ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.movieActorRepository = movieActorRepository;
        this.movieDirectorRepository = movieDirectorRepository;
        this.formatRepository = formatRepository;
        this.movieMediaService = movieMediaService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponse> getShowingMovies() {
        return movieRepository.findByStatus(MovieStatus.SHOWING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponse> getComingSoonMovies() {
        return movieRepository.findByStatus(MovieStatus.COMING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Movie not found"));
        return mapToResponse(movie);
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "MOVIE")
    public MovieResponse createMovie(MovieRequest request, MultipartFile poster) {
        try {
            Movie movie = new Movie();
            movie.setTitle(request.getTitle());
            movie.setDescription(request.getDescription());
            movie.setDuration(request.getDuration());
            movie.setStatus(request.getStatus());
            movie.setAgeRating(AgeRating.valueOf(request.getAgeRating()));
            movie.setRating(request.getRating());
            movie.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
            movie.setReleaseDate(request.getReleaseDate());

            if (poster != null && !poster.isEmpty()) {
                movie.setPosterUrl(movieMediaService.uploadPoster(poster));
            } else {
                movie.setPosterUrl(request.getPosterUrl());
            }
            movie.setTrailerUrl(request.getTrailerUrl());
            movie.setTmdbId(request.getTmdbId());

            if (request.getFormats() != null) {
                Set<Format> formats = request.getFormats().stream()
                        .map(name -> formatRepository.findByName(name)
                                .orElseGet(() -> formatRepository.save(new Format(null, name))))
                        .collect(Collectors.toSet());
                movie.setFormats(formats);
            }

            if (request.getGenres() != null) {
                Set<Genre> genres = request.getGenres().stream()
                        .map(name -> genreRepository.findByName(name)
                                .orElseGet(() -> genreRepository.save(new Genre(null, name))))
                        .collect(Collectors.toSet());
                movie.setGenres(genres);
            }

            Movie savedMovie = movieRepository.save(movie);
            saveCast(savedMovie, request);

            return mapToResponse(savedMovie);
        } catch (IOException e) {
            throw new AppException("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "MOVIE")
    public MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile poster) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new AppException("Movie not found"));

            movie.setTitle(request.getTitle());
            movie.setDescription(request.getDescription());
            movie.setDuration(request.getDuration());
            movie.setStatus(request.getStatus());
            movie.setAgeRating(AgeRating.valueOf(request.getAgeRating()));
            movie.setRating(request.getRating());
            movie.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
            movie.setReleaseDate(request.getReleaseDate());

            if (poster != null && !poster.isEmpty()) {
                movie.setPosterUrl(movieMediaService.uploadPoster(poster));
            } else if (request.getPosterUrl() != null) {
                movie.setPosterUrl(request.getPosterUrl());
            }

            movie.setTrailerUrl(request.getTrailerUrl());
            movie.setTmdbId(request.getTmdbId());

            if (request.getFormats() != null) {
                Set<Format> formats = request.getFormats().stream()
                        .map(name -> formatRepository.findByName(name)
                                .orElseGet(() -> formatRepository.save(new Format(null, name))))
                        .collect(Collectors.toSet());
                movie.setFormats(formats);
            }

            if (request.getGenres() != null) {
                Set<Genre> genres = request.getGenres().stream()
                        .map(name -> genreRepository.findByName(name)
                                .orElseGet(() -> genreRepository.save(new Genre(null, name))))
                        .collect(Collectors.toSet());
                movie.setGenres(genres);
            }

            movieDirectorRepository.deleteByMovieId(id);
            movieActorRepository.deleteByMovieId(id);
            saveCast(movie, request);

            return mapToResponse(movieRepository.save(movie));
        } catch (IOException e) {
            throw new AppException("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    private void saveCast(Movie movie, MovieRequest request) {
        if (request.getDirector() != null && !request.getDirector().isEmpty()) {
            Director director = directorRepository.findByName(request.getDirector())
                    .orElseGet(() -> {
                        Director d = new Director();
                        d.setName(request.getDirector());
                        d.setAvatarUrl(request.getDirectorAvatarUrl());
                        return directorRepository.save(d);
                    });
            movieDirectorRepository
                    .save(new MovieDirector(new MovieDirector.MovieDirectorId(movie.getId(), director.getId()), movie,
                            director, DirectorRole.MAIN));
        }

        if (request.getActors() != null && !request.getActors().isEmpty()) {
            String[] actors = request.getActors().split(",");
            String[] avatars = request.getActorAvatarUrls() != null ? request.getActorAvatarUrls().split(",")
                    : new String[0];
            for (int i = 0; i < actors.length; i++) {
                String actorName = actors[i].trim();
                String avatarUrl = i < avatars.length ? avatars[i].trim() : null;
                Actor actor = actorRepository.findByName(actorName)
                        .orElseGet(() -> {
                            Actor a = new Actor();
                            a.setName(actorName);
                            a.setAvatarUrl(avatarUrl);
                            return actorRepository.save(a);
                        });
                movieActorRepository.save(new MovieActor(new MovieActor.MovieActorId(movie.getId(), actor.getId()),
                        movie, actor, "N/A", i));
            }
        }
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new AppException("Movie not found");
        }
        movieActorRepository.deleteByMovieId(id);
        movieDirectorRepository.deleteByMovieId(id);
        movieRepository.deleteById(id);
    }

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = modelMapper.map(movie, MovieResponse.class);

        if (movie.getGenres() != null) {
            response.setGenres(movie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList()));
        }

        if (movie.getMovieDirectors() != null) {
            response.setDirectors(movie.getMovieDirectors().stream()
                    .map(md -> new PersonResponse(md.getDirector().getName(), md.getDirector().getAvatarUrl()))
                    .collect(Collectors.toList()));
        }

        if (movie.getMovieActors() != null) {
            response.setActors(movie.getMovieActors().stream()
                    .sorted(Comparator.comparing(MovieActor::getDisplayOrder))
                    .map(ma -> new PersonResponse(ma.getActor().getName(), ma.getActor().getAvatarUrl()))
                    .collect(Collectors.toList()));
        }

        response.setRating(movie.getRating() != null ? movie.getRating() : 0.0);
        response.setPriorityLevel(movie.getPriorityLevel() != null ? movie.getPriorityLevel() : 1);
        response.setAgeRating(movie.getAgeRating() != null ? movie.getAgeRating().name() : "P");
        response.setReleaseDate(movie.getReleaseDate());

        if (movie.getFormats() != null) {
            response.setFormats(movie.getFormats().stream()
                    .map(Format::getName)
                    .collect(Collectors.toList()));
        } else {
            response.setFormats(new ArrayList<>());
        }

        return response;
    }
}
