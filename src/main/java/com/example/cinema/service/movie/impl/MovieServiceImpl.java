package com.example.cinema.service.movie.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.dto.response.PersonResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.AgeRating;
import com.example.cinema.model.enums.DirectorRole;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.movie.*;
import com.example.cinema.service.movie.MovieMediaService;
import com.example.cinema.service.movie.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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
    private final MovieMediaService movieMediaService;
    private final ModelMapper modelMapper;

    public MovieServiceImpl(MovieRepository movieRepository, 
                            GenreRepository genreRepository,
                            ActorRepository actorRepository,
                            DirectorRepository directorRepository,
                            MovieActorRepository movieActorRepository,
                            MovieDirectorRepository movieDirectorRepository,
                            MovieMediaService movieMediaService,
                            ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.movieActorRepository = movieActorRepository;
        this.movieDirectorRepository = movieDirectorRepository;
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
    @Transactional
    @LogAction(action = "CREATE", target = "MOVIE")
    public MovieResponse createMovie(MovieRequest request, MultipartFile poster) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : MovieStatus.COMING);
        movie.setRating(request.getRating() != null ? request.getRating() : 0.0);
        
        try {
            movie.setAgeRating(AgeRating.valueOf(request.getAgeRating()));
        } catch (Exception e) {
            movie.setAgeRating(AgeRating.P);
        }
        
        if (poster != null && !poster.isEmpty()) {
            try {
                movie.setPosterUrl(movieMediaService.uploadPoster(poster));
            } catch (IOException e) {
                movie.setPosterUrl(request.getPosterUrl());
            }
        } else {
            movie.setPosterUrl(request.getPosterUrl());
        }
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setTmdbId(request.getTmdbId());

        if (request.getGenres() != null) {
            Set<Genre> genres = request.getGenres().stream()
                .map(name -> genreRepository.findByName(name)
                    .orElseGet(() -> genreRepository.save(new Genre(null, name))))
                .collect(Collectors.toSet());
            movie.setGenres(genres);
        }

        Movie savedMovie = movieRepository.save(movie);

        if (request.getDirector() != null && !request.getDirector().isEmpty()) {
            String[] directors = request.getDirector().split(",");
            String dirAvatar = request.getDirectorAvatarUrl();
            if (dirAvatar != null && !dirAvatar.startsWith("http")) dirAvatar = "https://image.tmdb.org/t/p/w200" + dirAvatar;

            for (String dName : directors) {
                String name = dName.trim();
                final String finalAvatar = dirAvatar;
                Director director = directorRepository.findByName(name)
                    .map(d -> {
                        if (d.getAvatarUrl() == null) d.setAvatarUrl(finalAvatar);
                        return directorRepository.save(d);
                    })
                    .orElseGet(() -> directorRepository.save(new Director(null, name, finalAvatar)));
                
                MovieDirector.MovieDirectorId mdId = new MovieDirector.MovieDirectorId(savedMovie.getId(), director.getId());
                movieDirectorRepository.save(new MovieDirector(mdId, savedMovie, director, DirectorRole.MAIN));
            }
        }

        if (request.getActors() != null && !request.getActors().isEmpty()) {
            String[] cast = request.getActors().split(",");
            String[] actorAvatars = request.getActorAvatarUrls() != null ? request.getActorAvatarUrls().split(",") : new String[0];
            
            for (int i = 0; i < cast.length; i++) {
                String name = cast[i].trim();
                String avatar = (i < actorAvatars.length && !actorAvatars[i].isEmpty()) ? actorAvatars[i].trim() : null;
                if (avatar != null && !avatar.startsWith("http")) avatar = "https://image.tmdb.org/t/p/w200" + avatar;
                
                final String finalAvatar = avatar;
                Actor actor = actorRepository.findByName(name)
                    .map(a -> {
                        if (a.getAvatarUrl() == null) a.setAvatarUrl(finalAvatar);
                        return actorRepository.save(a);
                    })
                    .orElseGet(() -> actorRepository.save(new Actor(null, name, finalAvatar)));
                
                MovieActor.MovieActorId maId = new MovieActor.MovieActorId(savedMovie.getId(), actor.getId());
                movieActorRepository.save(new MovieActor(maId, savedMovie, actor, "N/A", i));
            }
        }
        
        return mapToResponse(savedMovie);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "MOVIE")
    public MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile poster) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Movie not found"));
        
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : movie.getStatus());
        movie.setRating(request.getRating() != null ? request.getRating() : movie.getRating());
        
        if (poster != null && !poster.isEmpty()) {
            try {
                movie.setPosterUrl(movieMediaService.uploadPoster(poster));
            } catch (IOException e) {
                if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
            }
        } else if (request.getPosterUrl() != null) {
            movie.setPosterUrl(request.getPosterUrl());
        }
        
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setTmdbId(request.getTmdbId());

        // Update Genres
        if (request.getGenres() != null) {
            Set<Genre> genres = request.getGenres().stream()
                .map(name -> genreRepository.findByName(name)
                    .orElseGet(() -> genreRepository.save(new Genre(null, name))))
                .collect(Collectors.toSet());
            movie.setGenres(genres);
        }

        Movie savedMovie = movieRepository.save(movie);

        // Update Directors
        movieDirectorRepository.deleteByMovieId(id);
        if (request.getDirector() != null && !request.getDirector().isEmpty()) {
            String[] directors = request.getDirector().split(",");
            for (String dName : directors) {
                String name = dName.trim();
                Director director = directorRepository.findByName(name)
                    .orElseGet(() -> directorRepository.save(new Director(null, name, null)));
                
                MovieDirector.MovieDirectorId mdId = new MovieDirector.MovieDirectorId(savedMovie.getId(), director.getId());
                movieDirectorRepository.save(new MovieDirector(mdId, savedMovie, director, DirectorRole.MAIN));
            }
        }

        // Update Actors
        movieActorRepository.deleteByMovieId(id);
        if (request.getActors() != null && !request.getActors().isEmpty()) {
            String[] cast = request.getActors().split(",");
            for (int i = 0; i < cast.length; i++) {
                String name = cast[i].trim();
                Actor actor = actorRepository.findByName(name)
                    .orElseGet(() -> actorRepository.save(new Actor(null, name, null)));
                
                MovieActor.MovieActorId maId = new MovieActor.MovieActorId(savedMovie.getId(), actor.getId());
                movieActorRepository.save(new MovieActor(maId, savedMovie, actor, "N/A", i));
            }
        }
        
        return mapToResponse(savedMovie);
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "MOVIE")
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new AppException("Movie not found");
        }
        movieActorRepository.deleteByMovieId(id);
        movieDirectorRepository.deleteByMovieId(id);
        movieRepository.deleteById(id);
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

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = modelMapper.map(movie, MovieResponse.class);
        if (movie.getGenres() != null) {
            response.setGenres(movie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList()));
        }
        List<MovieActor> movieActors = movieActorRepository.findByMovieId(movie.getId());
        response.setActors(movieActors.stream()
                .map(ma -> new PersonResponse(ma.getActor().getName(), ma.getActor().getAvatarUrl()))
                .collect(Collectors.toList()));
        List<MovieDirector> movieDirectors = movieDirectorRepository.findByMovieId(movie.getId());
        response.setDirectors(movieDirectors.stream()
                .map(md -> new PersonResponse(md.getDirector().getName(), md.getDirector().getAvatarUrl()))
                .collect(Collectors.toList()));

        response.setRating(movie.getRating() != null ? movie.getRating() : 0.0);
        
        response.setAgeRating(movie.getAgeRating() != null ? movie.getAgeRating().name() : "P");
        response.setReleaseDate(movie.getReleaseDate());
        return response;
    }
}
