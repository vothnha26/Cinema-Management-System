package com.example.cinema.service.impl;

import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.MovieRepository;
import com.example.cinema.service.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final com.example.cinema.repository.GenreRepository genreRepository;
    private final com.example.cinema.repository.ActorRepository actorRepository;
    private final com.example.cinema.repository.DirectorRepository directorRepository;
    private final com.example.cinema.repository.MovieActorRepository movieActorRepository;
    private final com.example.cinema.repository.MovieDirectorRepository movieDirectorRepository;
    private final ModelMapper modelMapper;

    public MovieServiceImpl(MovieRepository movieRepository, 
                            com.example.cinema.repository.GenreRepository genreRepository,
                            com.example.cinema.repository.ActorRepository actorRepository,
                            com.example.cinema.repository.DirectorRepository directorRepository,
                            com.example.cinema.repository.MovieActorRepository movieActorRepository,
                            com.example.cinema.repository.MovieDirectorRepository movieDirectorRepository,
                            ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.movieActorRepository = movieActorRepository;
        this.movieDirectorRepository = movieDirectorRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public MovieResponse createMovie(com.example.cinema.model.dto.request.MovieRequest request) {
        System.out.println(">>> Service: Bắt đầu tạo phim: " + request.getTitle());
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : com.example.cinema.model.enums.MovieStatus.COMING);
        movie.setRating(String.valueOf(request.getRating()));
        movie.setAgeRating(request.getAgeRating());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setTmdbId(request.getTmdbId());

        // 1. Xử lý Genres
        System.out.println(">>> Service (1/3): Đang xử lý Genres...");
        if (request.getGenres() != null) {
            java.util.Set<com.example.cinema.model.entity.Genre> genres = request.getGenres().stream()
                .map(name -> genreRepository.findByName(name)
                    .orElseGet(() -> genreRepository.save(new com.example.cinema.model.entity.Genre(null, name))))
                .collect(java.util.stream.Collectors.toSet());
            movie.setGenres(genres);
        }

        System.out.println(">>> Service (2/3): Đang lưu phim vào bảng 'movies'...");
        Movie savedMovie = movieRepository.save(movie);
        System.out.println(">>> Service: Đã lưu phim thành công với ID: " + savedMovie.getId());

        // 2. Xử lý Đạo diễn
        System.out.println(">>> Service (3/3): Đang xử lý Đạo diễn & Diễn viên...");
        if (request.getDirector() != null && !request.getDirector().isEmpty()) {
            String[] directors = request.getDirector().split(",");
            String dirAvatar = request.getDirectorAvatarUrl();
            if (dirAvatar != null && !dirAvatar.startsWith("http")) dirAvatar = "https://image.tmdb.org/t/p/w200" + dirAvatar;

            for (String dName : directors) {
                String name = dName.trim();
                final String finalAvatar = dirAvatar;
                com.example.cinema.model.entity.Director director = directorRepository.findByName(name)
                    .map(d -> {
                        if (d.getAvatarUrl() == null) d.setAvatarUrl(finalAvatar);
                        return directorRepository.save(d);
                    })
                    .orElseGet(() -> directorRepository.save(new com.example.cinema.model.entity.Director(null, name, finalAvatar)));
                
                com.example.cinema.model.entity.MovieDirector.MovieDirectorId mdId = 
                    new com.example.cinema.model.entity.MovieDirector.MovieDirectorId(savedMovie.getId(), director.getId());
                movieDirectorRepository.save(new com.example.cinema.model.entity.MovieDirector(mdId, savedMovie, director, com.example.cinema.model.enums.DirectorRole.MAIN));
            }
        }

        // 3. Xử lý Diễn viên
        if (request.getActors() != null && !request.getActors().isEmpty()) {
            System.out.println(">>> [DEBUG SERVICE] Actors String: " + request.getActors());
            System.out.println(">>> [DEBUG SERVICE] Actor Avatars String: " + request.getActorAvatarUrls());
            
            String[] cast = request.getActors().split(",");
            String[] actorAvatars = request.getActorAvatarUrls() != null ? request.getActorAvatarUrls().split(",") : new String[0];
            
            for (int i = 0; i < cast.length; i++) {
                String name = cast[i].trim();
                String avatar = (i < actorAvatars.length && !actorAvatars[i].isEmpty()) ? actorAvatars[i].trim() : null;
                if (avatar != null && !avatar.startsWith("http")) avatar = "https://image.tmdb.org/t/p/w200" + avatar;
                
                System.out.println(">>> [DEBUG SERVICE] Đang xử lý Diễn viên: " + name + " | Avatar: " + avatar);
                
                final String finalAvatar = avatar;
                com.example.cinema.model.entity.Actor actor = actorRepository.findByName(name)
                    .map(a -> {
                        if (a.getAvatarUrl() == null) a.setAvatarUrl(finalAvatar);
                        return actorRepository.save(a);
                    })
                    .orElseGet(() -> actorRepository.save(new com.example.cinema.model.entity.Actor(null, name, finalAvatar)));
                
                com.example.cinema.model.entity.MovieActor.MovieActorId maId = 
                    new com.example.cinema.model.entity.MovieActor.MovieActorId(savedMovie.getId(), actor.getId());
                movieActorRepository.save(new com.example.cinema.model.entity.MovieActor(maId, savedMovie, actor, "N/A", i));
            }
        }
        
        System.out.println(">>> Service: Hoàn thành bóc tách và tạo liên kết thành công!");
        return mapToResponse(savedMovie);
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
                .orElseThrow(() -> new com.example.cinema.exception.AppException("Movie not found"));
        return mapToResponse(movie);
    }

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = modelMapper.map(movie, MovieResponse.class);
        
        // 1. Ánh xạ Thể loại
        if (movie.getGenres() != null) {
            response.setGenres(movie.getGenres().stream()
                    .map(g -> g.getName())
                    .collect(Collectors.toList()));
        }
        
        // 2. Nạp thêm Diễn viên (Actors)
        java.util.List<com.example.cinema.model.entity.MovieActor> movieActors = movieActorRepository.findByMovieId(movie.getId());
        response.setActors(movieActors.stream()
                .map(ma -> new com.example.cinema.model.dto.response.PersonResponse(
                        ma.getActor().getName(), 
                        ma.getActor().getAvatarUrl()))
                .collect(Collectors.toList()));
        
        // 3. Nạp thêm Đạo diễn (Directors)
        java.util.List<com.example.cinema.model.entity.MovieDirector> movieDirectors = movieDirectorRepository.findByMovieId(movie.getId());
        response.setDirectors(movieDirectors.stream()
                .map(md -> new com.example.cinema.model.dto.response.PersonResponse(
                        md.getDirector().getName(), 
                        md.getDirector().getAvatarUrl()))
                .collect(Collectors.toList()));

        // 4. Các thông tin Rating & ReleaseDate
        String rStr = movie.getRating();
        if (rStr != null && (rStr.startsWith("C") || rStr.equals("P"))) {
            // Trường hợp dữ liệu cũ: rating chứa C13, C16...
            response.setRating(0.0);
            if (response.getAgeRating() == null) response.setAgeRating(rStr);
        } else {
            try {
                response.setRating(rStr != null ? Double.parseDouble(rStr) : 0.0);
            } catch (Exception e) {
                response.setRating(0.0);
            }
        }
        
        if (response.getAgeRating() == null) {
            response.setAgeRating(movie.getAgeRating() != null ? movie.getAgeRating() : "P");
        }
        
        response.setReleaseDate(movie.getReleaseDate());
        
        return response;
    }
}
