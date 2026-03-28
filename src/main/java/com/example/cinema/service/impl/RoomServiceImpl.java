package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.enums.SeatType;
import com.example.cinema.repository.RoomRepository;
import com.example.cinema.repository.SeatRepository;
import com.example.cinema.service.RoomService;
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

    public RoomServiceImpl(RoomRepository roomRepository, SeatRepository seatRepository, ModelMapper modelMapper) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.modelMapper = modelMapper;
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
    public RoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setType(request.getType());
        room.setCapacity(request.getRows() * request.getCols());
        room.setStatus(true);

        Room savedRoom = roomRepository.save(room);

        // Tự động sinh ghế
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < request.getRows(); i++) {
            String rowChar = String.valueOf((char) ('A' + i));
            for (int j = 1; j <= request.getCols(); j++) {
                Seat seat = new Seat();
                seat.setRoom(savedRoom);
                seat.setRowChar(rowChar);
                seat.setColNum(j);
                
                // Mặc định: Hàng F, G là VIP (cho phòng 8 hàng), hoặc tùy chỉnh logic sau
                if (i >= 5 && i <= 6) {
                    seat.setType(SeatType.VIP);
                } else if (i == request.getRows() - 1) {
                    seat.setType(SeatType.COUPLE);
                } else {
                    seat.setType(SeatType.STANDARD);
                }
                
                seat.setStatus(true);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);

        RoomResponse response = modelMapper.map(savedRoom, RoomResponse.class);
        response.setSeats(seats.stream()
                .map(seat -> modelMapper.map(seat, RoomResponse.SeatResponse.class))
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new AppException("Không tìm thấy phòng với ID: " + id);
        }
        // Lưu ý: Trong thực tế cần kiểm tra xem phòng có đang có suất chiếu nào không
        seatRepository.deleteByRoomId(id);
        roomRepository.deleteById(id);
    }
}
