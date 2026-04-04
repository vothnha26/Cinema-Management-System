package com.example.cinema.service.room.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.request.SeatUpdateRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.service.room.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;
    private final com.example.cinema.service.room.strategy.SeatLayoutFactory seatLayoutFactory;

    public RoomServiceImpl(RoomRepository roomRepository, SeatRepository seatRepository, ModelMapper modelMapper,
            com.example.cinema.service.room.strategy.SeatLayoutFactory seatLayoutFactory) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.modelMapper = modelMapper;
        this.seatLayoutFactory = seatLayoutFactory;
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + id));

        List<Seat> seats = seatRepository.findByRoomId(id);
        RoomResponse response = modelMapper.map(room, RoomResponse.class);
        response.setSeats(seats.stream()
                .map(seat -> modelMapper.map(seat, RoomResponse.SeatResponse.class))
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "ROOM")
    public RoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setType(request.getType());
        room.setRows(request.getRows());
        room.setCols(request.getCols());
        room.setCapacity(request.getRows() * request.getCols());
        room.setStatus(true);

        Room savedRoom = roomRepository.save(room);

        List<Seat> seats = seatLayoutFactory.getStrategy(request.getType())
                .generateSeats(savedRoom, request.getRows(), request.getCols());

        seatRepository.saveAll(seats);

        RoomResponse response = modelMapper.map(savedRoom, RoomResponse.class);
        response.setSeats(seats.stream()
                .map(seat -> modelMapper.map(seat, RoomResponse.SeatResponse.class))
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "ROOM")
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + id));

        room.setName(request.getName());
        room.setType(request.getType());
        // Không cập nhật capacity/rows/cols ở đây để bảo toàn sơ đồ ghế

        Room updatedRoom = roomRepository.save(room);
        return getRoomById(updatedRoom.getId());
    }

    @Override
    @Transactional
    public RoomResponse updateSeats(Long roomId, com.example.cinema.model.dto.request.SeatBulkRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + roomId));

        List<Seat> updatedSeats = new ArrayList<>();
        for (SeatUpdateRequest req : request.getSeats()) {
            Seat seat = seatRepository.findByRoomIdAndRowCharAndColNum(roomId, req.getRowChar(), req.getColNum())
                    .orElseThrow(() -> new AppException(
                            "Không tìm thấy ghế " + req.getRowChar() + req.getColNum() + " trong phòng này"));

            seat.setType(req.getType());
            seat.setStatus(req.getStatus());
            updatedSeats.add(seat);
        }

        seatRepository.saveAll(updatedSeats);
        return getRoomById(roomId);
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "ROOM")
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new AppException("Không tìm thấy phòng với ID: " + id);
        }
        seatRepository.deleteByRoomId(id);
        roomRepository.deleteById(id);
    }
}
