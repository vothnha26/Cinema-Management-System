package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.BranchMovie;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.movie.BranchMovieRepository;
import com.example.cinema.repository.movie.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/branch-movies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBranchMovieController {

    @Autowired
    private BranchMovieRepository branchMovieRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private BranchRepository branchRepository;

    // Admin lấy tất cả các bản phân phối phim
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchMovie>>> getAllDistributions() {
        return ResponseEntity.ok(ApiResponse.ok(branchMovieRepository.findAll()));
    }

    // Admin phân phối phim xuống rạp
    @PostMapping
    public ResponseEntity<ApiResponse<BranchMovie>> distributeMovie(@RequestBody Map<String, Object> payload) {
        Long branchId = Long.valueOf(payload.get("branchId").toString());
        Long movieId = Long.valueOf(payload.get("movieId").toString());
        String status = payload.get("status") != null ? payload.get("status").toString() : "NOW_SHOWING";

        Branch branch = branchRepository.findById(branchId).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        BranchMovie bm = branchMovieRepository.findByBranchAndMovie(branch, movie).orElse(new BranchMovie());
        bm.setBranch(branch);
        bm.setMovie(movie);
        bm.setStatus(MovieStatus.valueOf(status));
        bm.setIsActive(true);

        return ResponseEntity.ok(ApiResponse.ok(branchMovieRepository.save(bm)));
    }

    // Xóa phim khỏi rạp
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeMovieFromBranch(@PathVariable Long id) {
        branchMovieRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // Manager lấy danh sách phim tại rạp mình
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<BranchMovie>>> getMoviesByBranch(@PathVariable Long branchId) {
        Branch branch = branchRepository.findById(branchId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(branchMovieRepository.findByBranch(branch)));
    }
}
