package com.example.cinema.service.room;

import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms();

    List<RoomResponse> getRoomsByBranch(Long branchId);

    RoomResponse getRoomById(Long id);

    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Long id, RoomRequest request);

    RoomResponse updateSeats(Long roomId, com.example.cinema.model.dto.request.SeatBulkRequest request);

    void deleteRoom(Long id);
}
