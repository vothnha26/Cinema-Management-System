package com.example.cinema.controller.public_api;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.Genre;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.movie.GenreRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class MetadataController {

    private final RoomTypeRepository roomTypeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final BranchRepository branchRepository;
    private final GenreRepository genreRepository;

    public MetadataController(RoomTypeRepository roomTypeRepository, 
                            SeatTypeRepository seatTypeRepository,
                            BranchRepository branchRepository,
                            GenreRepository genreRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.branchRepository = branchRepository;
        this.genreRepository = genreRepository;
    }

    @GetMapping("/room-types")
    public ResponseEntity<ApiResponse<List<RoomType>>> getRoomTypes() {
        return ResponseEntity.ok(ApiResponse.ok(roomTypeRepository.findAll()));
    }

    @GetMapping("/seat-types")
    public ResponseEntity<ApiResponse<List<SeatType>>> getSeatTypes() {
        return ResponseEntity.ok(ApiResponse.ok(seatTypeRepository.findAll()));
    }

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<Branch>>> getBranches() {
        return ResponseEntity.ok(ApiResponse.ok(branchRepository.findAll()));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<Genre>>> getGenres() {
        return ResponseEntity.ok(ApiResponse.ok(genreRepository.findAll()));
    }
}
