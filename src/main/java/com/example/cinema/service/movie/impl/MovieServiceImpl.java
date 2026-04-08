package com.example.cinema.service.movie.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.dto.response.PersonResponse;
import com.example.cinema.model.dto.response.FormatResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.AgeRating;
import com.example.cinema.model.enums.DirectorRole;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.movie.*;
import com.example.cinema.repository.branch.BranchRepository;
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
import java.util.List;
import java.util.Optional;
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
    private final BranchMovieRepository branchMovieRepository;
    private final BranchRepository branchRepository;
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
            BranchMovieRepository branchMovieRepository,
            BranchRepository branchRepository,
            MovieMediaService movieMediaService,
            ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.movieActorRepository = movieActorRepository;
        this.movieDirectorRepository = movieDirectorRepository;
        this.formatRepository = formatRepository;
        this.branchMovieRepository = branchMovieRepository;
        this.branchRepository = branchRepository;
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
    public List<MovieResponse> getMoviesByBranch(Long branchId) {
        return branchMovieRepository.findByBranchIdAndIsActiveTrue(branchId).stream()
                .map(bm -> {
                    MovieResponse resp = mapToResponse(bm.getMovie());
                    resp.setPriorityLevel(bm.getPriorityLevel());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMoviePriority(Long branchId, Long movieId, Integer priority) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException("Branch not found"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException("Movie not found"));
        BranchMovie bm = branchMovieRepository.findByBranchAndMovie(branch, movie)
                .orElseThrow(() -> new AppException("Movie is not assigned to this branch"));
        bm.setPriorityLevel(priority);
        branchMovieRepository.save(bm);
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
            updateMovieEntity(movie, request, poster);
            Movie savedMovie = movieRepository.save(movie);
            saveCast(savedMovie, request);
            saveMovieBranches(savedMovie, request.getBranchIds());
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
            updateMovieEntity(movie, request, poster);
            movieDirectorRepository.deleteByMovieId(id);
            movieActorRepository.deleteByMovieId(id);
            saveCast(movie, request);
            saveMovieBranches(movie, request.getBranchIds());
            return mapToResponse(movieRepository.save(movie));
        } catch (IOException e) {
            throw new AppException("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    private void updateMovieEntity(Movie movie, MovieRequest request, MultipartFile poster) throws IOException {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration() != null ? request.getDuration() : 0);
        movie.setStatus(request.getStatus() != null ? request.getStatus() : MovieStatus.COMING);

        if (request.getAgeRating() != null && !request.getAgeRating().isEmpty()) {
            try {
                movie.setAgeRating(AgeRating.valueOf(request.getAgeRating()));
            } catch (IllegalArgumentException e) {
                movie.setAgeRating(AgeRating.P);
            }
        } else {
            movie.setAgeRating(AgeRating.P);
        }

        movie.setRating(request.getRating() != null ? request.getRating() : 0.0);
        movie.setReleaseDate(request.getReleaseDate());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setTmdbId(request.getTmdbId());
        movie.setOriginCountry(request.getOriginCountry());

        if (poster != null && !poster.isEmpty()) {
            movie.setPosterUrl(movieMediaService.uploadPoster(poster));
        } else if (request.getPosterUrl() != null && !request.getPosterUrl().isEmpty()) {
            movie.setPosterUrl(request.getPosterUrl());
        }

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
    }

    private void saveMovieBranches(Movie movie, List<Long> branchIds) {
        List<BranchMovie> existing = branchMovieRepository.findByMovieId(movie.getId());
        for (BranchMovie bm : existing) {
            if (branchIds == null || !branchIds.contains(bm.getBranch().getId())) {
                bm.setIsActive(false);
            }
        }
        branchMovieRepository.saveAll(existing);

        if (branchIds != null && !branchIds.isEmpty()) {
            for (Long branchId : branchIds) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new AppException("Branch not found: " + branchId));
                BranchMovie bm = branchMovieRepository.findByBranchAndMovie(branch, movie)
                        .orElseGet(() -> {
                            BranchMovie newBm = new BranchMovie();
                            newBm.setBranch(branch);
                            newBm.setMovie(movie);
                            newBm.setPriorityLevel(1);
                            return newBm;
                        });
                bm.setStatus(movie.getStatus());
                bm.setIsActive(true);
                branchMovieRepository.save(bm);
            }
        }
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id))
            throw new AppException("Movie not found");
        movieRepository.deleteById(id);
    }

    private void saveCast(Movie movie, MovieRequest request) {
        if (request.getDirector() != null && !request.getDirector().isEmpty()) {
            String[] directors = request.getDirector().split(",");
            String[] dAvatars = request.getDirectorAvatarUrl() != null ? request.getDirectorAvatarUrl().split(",")
                    : new String[0];
            for (int i = 0; i < directors.length; i++) {
                final String dName = directors[i].trim();
                final String dAvt = i < dAvatars.length ? dAvatars[i].trim() : null;
                Director director = directorRepository.findByName(dName).orElseGet(() -> {
                    Director d = new Director();
                    d.setName(dName);
                    d.setAvatarUrl(dAvt);
                    return directorRepository.save(d);
                });
                if (director.getAvatarUrl() == null && dAvt != null) {
                    director.setAvatarUrl(dAvt);
                    directorRepository.save(director);
                }
                MovieDirector md = new MovieDirector();
                md.setMovie(movie);
                md.setDirector(director);
                md.setRole(DirectorRole.MAIN);
                movieDirectorRepository.save(md);
            }
        }

        if (request.getActors() != null && !request.getActors().isEmpty()) {
            String[] actors = request.getActors().split(",");
            String[] aAvatars = request.getActorAvatarUrls() != null ? request.getActorAvatarUrls().split(",")
                    : new String[0];
            for (int i = 0; i < actors.length; i++) {
                final String aName = actors[i].trim();
                final String aAvt = i < aAvatars.length ? aAvatars[i].trim() : null;
                Actor actor = actorRepository.findByName(aName).orElseGet(() -> {
                    Actor a = new Actor();
                    a.setName(aName);
                    a.setAvatarUrl(aAvt);
                    return actorRepository.save(a);
                });
                if (actor.getAvatarUrl() == null && aAvt != null) {
                    actor.setAvatarUrl(aAvt);
                    actorRepository.save(actor);
                }
                MovieActor ma = new MovieActor();
                ma.setMovie(movie);
                ma.setActor(actor);
                movieActorRepository.save(ma);
            }
        }
    }

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse res = new MovieResponse();
        res.setId(movie.getId());
        res.setTitle(movie.getTitle());
        res.setDescription(movie.getDescription());
        res.setDuration(movie.getDuration());
        res.setPosterUrl(movie.getPosterUrl());
        res.setTrailerUrl(movie.getTrailerUrl());
        res.setStatus(movie.getStatus());
        res.setRating(movie.getRating());
        res.setTmdbId(movie.getTmdbId());
        res.setOriginCountry(movie.getOriginCountry());
        res.setReleaseDate(movie.getReleaseDate());
        res.setAgeRating(movie.getAgeRating() != null ? movie.getAgeRating().name() : "P");
        res.setGenres(movie.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
        res.setFormats(movie.getFormats().stream()
                .map(f -> new FormatResponse(f.getId(), f.getName(), f.getDescription())).collect(Collectors.toList()));
        res.setDirectors(movie.getMovieDirectors().stream().map(md -> new PersonResponse(md.getDirector().getId(),
                md.getDirector().getName(), md.getDirector().getAvatarUrl())).collect(Collectors.toList()));
        res.setActors(movie.getMovieActors().stream().map(
                ma -> new PersonResponse(ma.getActor().getId(), ma.getActor().getName(), ma.getActor().getAvatarUrl()))
                .collect(Collectors.toList()));
        res.setBranchIds(branchMovieRepository.findByMovieId(movie.getId()).stream()
                .filter(bm -> Boolean.TRUE.equals(bm.getIsActive())).map(bm -> bm.getBranch().getId())
                .collect(Collectors.toList()));
        return res;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updatePriority(Long id, Integer priority) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException("Movie not found"));
        movie.setPriorityLevel(priority);
        movieRepository.save(movie);
    }
}
