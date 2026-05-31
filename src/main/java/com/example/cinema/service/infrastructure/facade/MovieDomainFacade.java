package com.example.cinema.service.infrastructure.facade;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.movie.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class MovieDomainFacade {
    private final MovieRepository movieRepo;
    private final ActorRepository actorRepo;
    private final DirectorRepository directorRepo;
    private final GenreRepository genreRepo;
    private final FormatRepository formatRepo;
    private final BranchMovieRepository branchMovieRepo;

    public MovieDomainFacade(MovieRepository movieRepo, ActorRepository actorRepo, DirectorRepository directorRepo, 
                            GenreRepository genreRepo, FormatRepository formatRepo, BranchMovieRepository branchMovieRepo) {
        this.movieRepo = movieRepo;
        this.actorRepo = actorRepo;
        this.directorRepo = directorRepo;
        this.genreRepo = genreRepo;
        this.formatRepo = formatRepo;
        this.branchMovieRepo = branchMovieRepo;
    }

    public List<Movie> findAllMovies() { return movieRepo.findAll(); }
    public Optional<Movie> findMovie(Long id) { return movieRepo.findById(id); }
    public Movie saveMovie(Movie m) { return movieRepo.save(m); }
    public void deleteMovie(Long id) { movieRepo.deleteById(id); }
    
    public List<Actor> findAllActors() { return actorRepo.findAll(); }
    public Optional<Actor> findActor(Long id) { return actorRepo.findById(id); }
    public Actor saveActor(Actor a) { return actorRepo.save(a); }
    
    public List<Director> findAllDirectors() { return directorRepo.findAll(); }
    public Optional<Director> findDirector(Long id) { return directorRepo.findById(id); }
    public Director saveDirector(Director d) { return directorRepo.save(d); }
    
    public List<Genre> findAllGenres() { return genreRepo.findAll(); }
    public Optional<Genre> findGenre(Long id) { return genreRepo.findById(id); }
    
    public List<Format> findAllFormats() { return formatRepo.findAll(); }
    public Optional<Format> findFormat(Long id) { return formatRepo.findById(id); }
    
    public boolean isMovieDistributedAtBranch(Long bId, Long mId) { return branchMovieRepo.existsByBranchIdAndMovieIdAndIsActiveTrue(bId, mId); }
    public List<BranchMovie> findMoviesByBranchId(Long branchId) { return branchMovieRepo.findByBranchIdAndIsActiveTrue(branchId); }
    public Optional<BranchMovie> findBranchMovie(Long bId, Long mId) { return branchMovieRepo.findByBranchIdAndMovieIdAndIsActiveTrue(bId, mId); }
    public void saveBranchMovie(BranchMovie bm) { branchMovieRepo.save(bm); }
}
