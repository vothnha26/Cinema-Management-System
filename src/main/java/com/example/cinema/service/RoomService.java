package com.example.cinema.service;

import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms();
    RoomResponse getRoomById(Long id);
    RoomResponse createRoom(RoomRequest request);
    void deleteRoom(Long id);
}
