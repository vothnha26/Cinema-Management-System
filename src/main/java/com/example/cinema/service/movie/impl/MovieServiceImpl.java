package com.example.cinema.service.movie.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.service.movie.MovieService;
import com.example.cinema.service.infrastructure.facade.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieDomainFacade movieRepo;
    private final CinemaDomainFacade cinemaRepo;
    private final MediaFacade mediaFacade;
    private final ModelMapper modelMapper;

    public MovieServiceImpl(MovieDomainFacade movieRepo, CinemaDomainFacade cinemaRepo, 
                            MediaFacade mediaFacade, ModelMapper modelMapper) {
        this.movieRepo = movieRepo;
        this.cinemaRepo = cinemaRepo;
        this.mediaFacade = mediaFacade;
        this.modelMapper = modelMapper;
    }

    @Override public List<MovieResponse> getAllMovies() { return movieRepo.findAllMovies().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public List<MovieResponse> getShowingMovies() { return getAllMovies(); }
    @Override public List<MovieResponse> getComingSoonMovies() { return getAllMovies(); }
    @Override public MovieResponse getMovieById(Long id) { return movieRepo.findMovie(id).map(this::mapToResponse).orElseThrow(() -> new AppException("Phim không tồn tại")); }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "MOVIE")
    public MovieResponse createMovie(MovieRequest request, MultipartFile file) {
        Movie movie = modelMapper.map(request, Movie.class);
        if (file != null && !file.isEmpty()) movie.setPosterUrl(mediaFacade.uploadMoviePoster(file));
        movie.setGenres(request.getGenreIds().stream().map(id -> movieRepo.findGenre(id).orElseThrow()).collect(Collectors.toSet()));
        Movie saved = movieRepo.saveMovie(movie);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "MOVIE")
    public MovieResponse updateMovie(Long id, MovieRequest request, MultipartFile file) {
        Movie movie = movieRepo.findMovie(id).orElseThrow(() -> new AppException("Phim không tồn tại"));
        
        // Lưu ID cũ
        Long originalId = movie.getId();
        
        // Map dữ liệu mới vào thực thể
        modelMapper.map(request, movie);
        
        // Đảm bảo ID không bị thay đổi bởi ModelMapper (nếu có trường ID trong request hoặc trùng tên)
        movie.setId(originalId);
        
        if (file != null && !file.isEmpty()) {
            movie.setPosterUrl(mediaFacade.uploadMoviePoster(file));
        }
        
        return mapToResponse(movieRepo.saveMovie(movie));
    }

    @Override
    @Transactional
    public void updateMoviePriority(Long branchId, Long movieId, Integer priority) {
        if (branchId != null) {
            BranchMovie bm = movieRepo.findBranchMovie(branchId, movieId)
                .orElseThrow(() -> new AppException("Phim chưa được phân bổ vào chi nhánh này"));
            bm.setPriorityLevel(priority);
            movieRepo.saveBranchMovie(bm);
        } else {
            Movie m = movieRepo.findMovie(movieId).orElseThrow();
            m.setPriorityLevel(priority);
            movieRepo.saveMovie(m);
        }
    }

    @Override
    public List<MovieResponse> getMoviesByBranch(Long branchId) {
        return movieRepo.findMoviesByBranchId(branchId).stream()
            .map(bm -> {
                MovieResponse res = mapToResponse(bm.getMovie());
                res.setStatus(bm.getStatus());
                res.setPriorityLevel(bm.getPriorityLevel());
                return res;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "MOVIE")
    public void deleteMovie(Long id) { movieRepo.deleteMovie(id); }

    private MovieResponse mapToResponse(Movie m) {
        MovieResponse res = modelMapper.map(m, MovieResponse.class);
        if (m.getFormats() != null) {
            res.setFormats(m.getFormats().stream()
                .map(f -> modelMapper.map(f, com.example.cinema.model.dto.response.FormatResponse.class))
                .collect(Collectors.toList()));
        }
        if (m.getMovieActors() != null) {
            res.setActors(m.getMovieActors().stream()
                .map(ma -> modelMapper.map(ma.getActor(), com.example.cinema.model.dto.response.PersonResponse.class))
                .collect(Collectors.toList()));
        }
        if (m.getMovieDirectors() != null) {
            res.setDirectors(m.getMovieDirectors().stream()
                .map(md -> modelMapper.map(md.getDirector(), com.example.cinema.model.dto.response.PersonResponse.class))
                .collect(Collectors.toList()));
        }
        return res;
    }
}
