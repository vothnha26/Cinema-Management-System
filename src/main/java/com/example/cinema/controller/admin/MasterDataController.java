package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/master-data")
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final RoomTypeRepository roomTypeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final com.example.cinema.repository.movie.FormatRepository formatRepository;

    public MasterDataController(RoomTypeRepository roomTypeRepository, 
                                SeatTypeRepository seatTypeRepository,
                                com.example.cinema.repository.movie.FormatRepository formatRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.formatRepository = formatRepository;
    }

    @GetMapping("/room-types")
    public ResponseEntity<ApiResponse<List<RoomType>>> getRoomTypes() {
        return ResponseEntity.ok(ApiResponse.ok(roomTypeRepository.findAll()));
    }

    @GetMapping("/seat-types")
    public ResponseEntity<ApiResponse<List<SeatType>>> getSeatTypes() {
        return ResponseEntity.ok(ApiResponse.ok(seatTypeRepository.findAll()));
    }

    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<List<com.example.cinema.model.entity.Format>>> getFormats() {
        return ResponseEntity.ok(ApiResponse.ok(formatRepository.findAll()));
    }
}
