package com.example.cinema.repository.movie;

import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.BranchMovie;
import com.example.cinema.model.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchMovieRepository extends JpaRepository<BranchMovie, Long> {
    List<BranchMovie> findByBranch(Branch branch);
    Optional<BranchMovie> findByBranchAndMovie(Branch branch, Movie movie);
    List<BranchMovie> findByMovieId(Long movieId);
    List<BranchMovie> findByBranchIdAndIsActiveTrue(Long branchId);
    boolean existsByBranchIdAndMovieIdAndIsActiveTrue(Long branchId, Long movieId);
}
