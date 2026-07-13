package com.example.cinema.service.infrastructure.facade;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.room.*;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class CinemaDomainFacade {
    private final RoomRepository roomRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final SeatRepository seatRepo;
    private final SeatTypeRepository seatTypeRepo;
    private final SeatPriceRepository seatPriceRepo;
    private final ShowtimeRepository showtimeRepo;

    public CinemaDomainFacade(RoomRepository roomRepo, RoomTypeRepository roomTypeRepo, SeatRepository seatRepo, 
                             SeatTypeRepository seatTypeRepo, SeatPriceRepository seatPriceRepo, 
                             ShowtimeRepository showtimeRepo) {
        this.roomRepo = roomRepo;
        this.roomTypeRepo = roomTypeRepo;
        this.seatRepo = seatRepo;
        this.seatTypeRepo = seatTypeRepo;
        this.seatPriceRepo = seatPriceRepo;
        this.showtimeRepo = showtimeRepo;
    }

    public List<Room> findAllRooms() { return roomRepo.findAll(); }
    public List<Room> findRoomsByBranch(Long branchId) { return roomRepo.findByBranchId(branchId); }
    public Optional<Room> findRoom(Long id) { return roomRepo.findById(id); }
    public Room saveRoom(Room r) { return roomRepo.save(r); }
    public void deleteRoom(Long id) { roomRepo.deleteById(id); }
    
    public List<RoomType> findAllRoomTypes() { return roomTypeRepo.findAll(); }
    public Optional<RoomType> findRoomType(String code) { return roomTypeRepo.findByCode(code); }
    public RoomType saveRoomType(RoomType rt) { return roomTypeRepo.save(rt); }
    
    public List<SeatType> findAllSeatTypes() { return seatTypeRepo.findAll(); }
    public Optional<SeatType> findSeatType(String code) { return seatTypeRepo.findByCode(code); }
    public SeatType saveSeatType(SeatType st) { return seatTypeRepo.save(st); }
    
    public List<Seat> findSeatsByRoom(Long roomId) { return seatRepo.findByRoomId(roomId); }
    public Optional<Seat> findSeat(Long id) { return seatRepo.findById(id); }
    
    public List<SeatPrice> findSeatPrice(RoomType rt, SeatType st) { return seatPriceRepo.findByRoomTypeAndSeatType(rt, st); }
    public Optional<SeatPrice> findActiveSeatPrice(RoomType rt, SeatType st) { return seatPriceRepo.findByRoomTypeAndSeatTypeAndIsActiveTrue(rt, st).stream().findFirst(); }
    public SeatPrice saveSeatPrice(SeatPrice sp) { return seatPriceRepo.save(sp); }

    public Optional<Showtime> findShowtime(Long id) { return showtimeRepo.findById(id); }
    public List<Showtime> findShowtimesByBranchAndDate(Long branchId, java.time.LocalDateTime start, java.time.LocalDateTime end) { return showtimeRepo.findByBranchIdAndStartTimeBetween(branchId, start, end); }
    public List<Showtime> findShowtimesByDate(java.time.LocalDateTime start, java.time.LocalDateTime end, Long branchId) { return showtimeRepo.findAllByStartTimeBetween(start, end, branchId); }
    public List<java.time.LocalDate> findDistinctShowtimeDates(Long movieId, Long branchId) { return showtimeRepo.findDistinctDates(movieId, branchId); }
    public Showtime saveShowtime(Showtime s) { return showtimeRepo.save(s); }
    public void deleteShowtime(Long id) { showtimeRepo.deleteById(id); }
}
