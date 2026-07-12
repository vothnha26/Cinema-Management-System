package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.entity.Format;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import com.example.cinema.repository.movie.FormatRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/master-data")
@CrossOrigin(origins = "*")
public class AdminMasterDataController {

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private SeatTypeRepository seatTypeRepository;

    // --- FORMATS ---
    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<List<Format>>> getAllFormats() {
        return ResponseEntity.ok(ApiResponse.ok(formatRepository.findAll()));
    }

    @PostMapping("/formats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Format>> createFormat(@RequestBody Format format) {
        return ResponseEntity.ok(ApiResponse.ok(formatRepository.save(format)));
    }

    @PutMapping("/formats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Format>> updateFormat(@PathVariable Long id, @RequestBody Format formatDetails) {
        Format format = formatRepository.findById(id).orElseThrow();
        format.setName(formatDetails.getName());
        format.setDescription(formatDetails.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(formatRepository.save(format)));
    }

    @DeleteMapping("/formats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFormat(@PathVariable Long id) {
        formatRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- ROOM TYPES ---
    @GetMapping("/room-types")
    public ResponseEntity<ApiResponse<List<RoomType>>> getAllRoomTypes() {
        return ResponseEntity.ok(ApiResponse.ok(roomTypeRepository.findAll()));
    }

    @PostMapping("/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomType>> createRoomType(@RequestBody RoomType roomType) {
        return ResponseEntity.ok(ApiResponse.ok(roomTypeRepository.save(roomType)));
    }

    @PutMapping("/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomType>> updateRoomType(@PathVariable String id, @RequestBody RoomType roomTypeDetails) {
        RoomType roomType = roomTypeRepository.findById(id).orElseThrow();
        roomType.setName(roomTypeDetails.getName());
        roomType.setSupportedFormats(roomTypeDetails.getSupportedFormats());
        return ResponseEntity.ok(ApiResponse.ok(roomTypeRepository.save(roomType)));
    }

    @DeleteMapping("/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable String id) {
        roomTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- SEAT TYPES ---
    @GetMapping("/seat-types")
    public ResponseEntity<ApiResponse<List<SeatType>>> getAllSeatTypes() {
        return ResponseEntity.ok(ApiResponse.ok(seatTypeRepository.findAll()));
    }
}
