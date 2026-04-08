package com.example.cinema.service.room.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.request.RoomTemplateRequest;
import com.example.cinema.model.dto.request.SeatUpdateRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.model.entity.Format;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.enums.RoomStatus;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.model.entity.Staff;
import com.example.cinema.model.enums.Role;
import com.example.cinema.service.room.RoomService;
import com.example.cinema.service.room.RoomTemplateService;
import com.example.cinema.service.room.strategy.SeatLayoutFactory;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ModelMapper modelMapper;
    private final SeatLayoutFactory seatLayoutFactory;
    private final RoomTemplateService roomTemplateService;
    private final RoomTypeRepository roomTypeRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public RoomServiceImpl(RoomRepository roomRepository, SeatRepository seatRepository,
            SeatTypeRepository seatTypeRepository,
            ShowtimeRepository showtimeRepository,
            BookingDetailRepository bookingDetailRepository,
            ModelMapper modelMapper,
            SeatLayoutFactory seatLayoutFactory,
            RoomTemplateService roomTemplateService,
            RoomTypeRepository roomTypeRepository,
            StaffRepository staffRepository,
            UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.showtimeRepository = showtimeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.modelMapper = modelMapper;
        this.seatLayoutFactory = seatLayoutFactory;
        this.roomTemplateService = roomTemplateService;
        this.roomTypeRepository = roomTypeRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentBranchId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .flatMap(user -> staffRepository.findByUserId(user.getId()))
                    .map(staff -> staff.getBranch().getId())
                    .orElse(null);
        }
        return null;
    }

    private boolean isManager() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
    }

    private RoomResponse mapToResponse(Room room) {
        RoomResponse response = modelMapper.map(room, RoomResponse.class);
        if (room.getBranch() != null) {
            response.setBranchId(room.getBranch().getId());
        }
        if (room.getRoomType() != null && room.getRoomType().getSupportedFormats() != null) {
            response.setSupportedFormats(room.getRoomType().getSupportedFormats().stream()
                    .map(Format::getName).collect(Collectors.toList()));
            response.setRoomTypeName(room.getRoomType().getName());
        }
        return response;
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        Long branchId = getCurrentBranchId();
        List<Room> rooms = (branchId != null && isManager()) 
            ? roomRepository.findByBranchId(branchId)
            : roomRepository.findAll();

        return rooms.stream()
                .map(room -> {
                    RoomResponse res = mapToResponse(room);
                    List<Seat> seats = seatRepository.findByRoomId(room.getId());
                    res.setSeats(seats.stream()
                            .map(seat -> modelMapper.map(seat, RoomResponse.SeatResponse.class))
                            .collect(Collectors.toList()));
                    res.setHasShowtime(showtimeRepository.existsByRoomId(room.getId()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + id));

        RoomResponse response = mapToResponse(room);
        List<Seat> seats = seatRepository.findByRoomId(id);
        response.setSeats(seats.stream()
                .map(seat -> modelMapper.map(seat, RoomResponse.SeatResponse.class))
                .collect(Collectors.toList()));
        response.setHasShowtime(showtimeRepository.existsByRoomId(id));

        return response;
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "ROOM")
    public RoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        
        // Auto-assign branch for Manager if not provided
        Long branchId = request.getBranchId();
        if (branchId == null && isManager()) {
            branchId = getCurrentBranchId();
        }
        
        if (branchId == null) {
            throw new AppException("Branch ID is required for room creation");
        }
        
        com.example.cinema.model.entity.Branch branch = new com.example.cinema.model.entity.Branch();
        branch.setId(branchId);
        room.setBranch(branch);

        com.example.cinema.model.entity.RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException("Room type not found: " + request.getRoomTypeId()));
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.ACTIVE);
        
        List<Seat> seats;
        
        if (request.getTemplateFileName() != null && !request.getTemplateFileName().isEmpty()) {
            RoomTemplateRequest template = roomTemplateService.getTemplate(request.getTemplateFileName());
            room.setRows(template.getRows());
            room.setCols(template.getCols());
            room.setCapacity(template.getSeats().size());
            
            Room savedRoom = roomRepository.save(room);
            
            seats = template.getSeats().stream().map(s -> {
                Seat seat = new Seat();
                seat.setRoom(savedRoom);
                seat.setRowChar(s.getRowChar());
                seat.setColNum(s.getColNum());
                
                com.example.cinema.model.entity.SeatType st = seatTypeRepository.findById(s.getSeatTypeId())
                        .orElseThrow(() -> new AppException("Seat type not found: " + s.getSeatTypeId()));
                seat.setSeatType(st);
                
                seat.setStatus(s.getStatus());
                return seat;
            }).collect(Collectors.toList());
            
        } else {
            if (request.getRows() == null || request.getCols() == null) {
                throw new AppException("Số hàng và số cột là bắt buộc khi không dùng template");
            }
            room.setRows(request.getRows());
            room.setCols(request.getCols());
            room.setCapacity(request.getRows() * request.getCols());
            
            Room savedRoom = roomRepository.save(room);
            
            seats = seatLayoutFactory.getStrategy(request.getRoomTypeId())
                    .generateSeats(savedRoom, request.getRows(), request.getCols());
        }

        seatRepository.saveAll(seats);
        return getRoomById(room.getId());
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "ROOM")
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + id));

        room.setName(request.getName());
        if (!room.getRoomType().getId().equals(request.getRoomTypeId())) {
            com.example.cinema.model.entity.RoomType newType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new AppException("Room type not found: " + request.getRoomTypeId()));
            room.setRoomType(newType);
        }

        Room updatedRoom = roomRepository.save(room);
        return getRoomById(updatedRoom.getId());
    }

    @Override
    @Transactional
    public RoomResponse updateSeats(Long roomId, com.example.cinema.model.dto.request.SeatBulkRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với ID: " + roomId));

        if (showtimeRepository.existsByRoomId(roomId)) {
            throw new AppException("Không thể thay đổi kết cấu phòng vì đang có lịch chiếu hoạt động. Vui lòng xóa lịch chiếu trước!");
        }

        bookingDetailRepository.updateSeatToNullByRoomId(roomId);
        seatRepository.deleteByRoomId(roomId);

        List<Seat> newSeats = new ArrayList<>();
        int activeSeatsCount = 0;

        for (SeatUpdateRequest req : request.getSeats()) {
            if ("EMPTY".equals(req.getSeatTypeId())) {
                continue; // Lối đi, không lưu vào DB
            }

            Seat seat = new Seat();
            seat.setRoom(room);
            seat.setRowChar(req.getRowChar());
            seat.setColNum(req.getColNum());

            com.example.cinema.model.entity.SeatType st = seatTypeRepository.findById(req.getSeatTypeId())
                    .orElseThrow(() -> new AppException("Seat type not found: " + req.getSeatTypeId()));
            seat.setSeatType(st);
            seat.setStatus(req.getStatus());

            newSeats.add(seat);
            if (!"DISABLED".equals(req.getSeatTypeId()) && Boolean.TRUE.equals(req.getStatus())) {
                activeSeatsCount++;
            }
        }
        if (request.getRows() != null) room.setRows(request.getRows());
        if (request.getCols() != null) room.setCols(request.getCols());
        room.setCapacity(activeSeatsCount);

        roomRepository.save(room);
        seatRepository.saveAll(newSeats);

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
