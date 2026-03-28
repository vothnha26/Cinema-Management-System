package com.example.cinema.controller.room;

import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.request.SeatBulkRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.service.room.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getAllRooms()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getRoomById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@RequestBody @Valid RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(roomService.createRoom(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id, 
            @RequestBody @Valid RoomRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.updateRoom(id, request)));
    }

    @PutMapping("/{id}/layout")
    public ResponseEntity<ApiResponse<RoomResponse>> updateLayout(
            @PathVariable Long id, 
            @RequestBody SeatBulkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.updateSeats(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
