package com.example.cinema.service.room.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.constant.AppConstants;
import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.request.SeatBulkRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.service.room.RoomService;
import com.example.cinema.service.infrastructure.facade.CinemaDomainFacade;
import com.example.cinema.service.infrastructure.facade.UserDomainFacade;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final CinemaDomainFacade cinemaRepo;
    private final UserDomainFacade userRepo;
    private final ModelMapper modelMapper;

    public RoomServiceImpl(CinemaDomainFacade cinemaRepo, UserDomainFacade userRepo, ModelMapper modelMapper) {
        this.cinemaRepo = cinemaRepo;
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
    }

    @Override public List<RoomResponse> getAllRooms() { return cinemaRepo.findAllRooms().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public List<RoomResponse> getRoomsByBranch(Long branchId) { return cinemaRepo.findRoomsByBranch(branchId).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public RoomResponse getRoomById(Long id) { return cinemaRepo.findRoom(id).map(this::mapToResponse).orElseThrow(() -> new AppException(AppConstants.MSG_NOT_FOUND)); }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Room room = modelMapper.map(request, Room.class);
        RoomType rt = cinemaRepo.findRoomType(request.getRoomTypeId()).orElseThrow();
        room.setRoomType(rt);
        room.setBranch(getCurrentStaff().getBranch());
        room.setStatus(com.example.cinema.model.enums.RoomStatus.valueOf(AppConstants.ROOM_STATUS_ACTIVE));
        return mapToResponse(cinemaRepo.saveRoom(room));
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = cinemaRepo.findRoom(id).orElseThrow();
        modelMapper.map(request, room);
        if (request.getRoomTypeId() != null) room.setRoomType(cinemaRepo.findRoomType(request.getRoomTypeId()).orElseThrow());
        return mapToResponse(cinemaRepo.saveRoom(room));
    }

    @Override @Transactional public void deleteRoom(Long id) { cinemaRepo.deleteRoom(id); }

    @Override
    public RoomResponse updateSeats(Long roomId, SeatBulkRequest request) {
        // Implementation for updating seats logic
        return null; 
    }

    private Staff getCurrentStaff() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) throw new AppException(AppConstants.MSG_UNAUTHORIZED);
        User user = userRepo.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return userRepo.findStaffByUser(user).orElseThrow(() -> new AppException("Staff profile not found"));
    }

    private RoomResponse mapToResponse(Room r) {
        RoomResponse res = modelMapper.map(r, RoomResponse.class);
        if (r.getRoomType() != null) {
            res.setRoomTypeId(r.getRoomType().getCode());
            res.setRoomTypeName(r.getRoomType().getName());
        }
        if (r.getBranch() != null) {
            res.setBranchId(r.getBranch().getId());
            res.setBranchName(r.getBranch().getName());
        }
        if (r.getSupportedFormats() != null) {
            res.setSupportedFormats(r.getSupportedFormats().stream()
                .map(f -> f.getName())
                .collect(Collectors.toList()));
        }
        if (r.getSeats() != null) {
            res.setSeats(r.getSeats().stream().map(s -> {
                RoomResponse.SeatResponse sr = modelMapper.map(s, RoomResponse.SeatResponse.class);
                if (s.getSeatType() != null) {
                    sr.setSeatTypeId(s.getSeatType().getCode());
                    sr.setSeatTypeName(s.getSeatType().getName());
                }
                sr.setStatus(s.getStatus() == com.example.cinema.model.enums.SeatStatus.ACTIVE);
                return sr;
            }).collect(Collectors.toList()));
        }
        return res;
    }
}
