package com.example.cinema.service.showtime.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.showtime.ShowtimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, 
                                SeatRepository seatRepository,
                                BookingDetailRepository bookingDetailRepository, 
                                SeatPriceRepository seatPriceRepository,
                                MovieRepository movieRepository,
                                RoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.seatPriceRepository = seatPriceRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<ShowtimeResponse> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieIdAndStatus(movieId, ShowtimeStatus.UPCOMING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatStatusForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException("Showtime not found"));

        Room room = showtime.getRoom();
        List<Seat> seats = seatRepository.findByRoomIdAndStatusTrue(room.getId());

        List<Long> bookedSeatIds = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);

        return seats.stream().map(seat -> {
            SeatResponse response = new SeatResponse();
            response.setId(seat.getId());
            response.setSeatCode(seat.getSeatCode());
            response.setSeatType(seat.getType());
            response.setRowChar(seat.getRowChar());
            response.setColNum(seat.getColNum());
            response.setAvailable(!bookedSeatIds.contains(seat.getId()));

            SeatPrice seatPrice = seatPriceRepository.findLatestPrice(
                    room.getType(), seat.getType(), java.time.LocalDate.now()).orElse(null);

            BigDecimal price = BigDecimal.valueOf(seat.getType().name().equals("VIP") ? 80000 : 60000);
            if (seatPrice != null) {
                price = seatPrice.getPrice();
            }
            response.setPrice(price);

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Phim không tồn tại"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Phòng không tồn tại"));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        
        // Chặn trùng lịch (Bao gồm 15p dọn dẹp)
        // Khoảng thời gian cần kiểm tra: [S - 15p, E + 15p]
        LocalDateTime checkStart = request.getStartTime().minusMinutes(15);
        LocalDateTime checkEnd = request.getStartTime().plusMinutes(movie.getDuration() + 15);
        
        List<Showtime> overlaps = showtimeRepository.findOverlappingShowtimes(
                room.getId(), checkStart, checkEnd);
        if (!overlaps.isEmpty()) {
            throw new AppException("Xung đột thời gian: Suất chiếu này chồng lấn với suất chiếu khác hoặc khoảng hở dọn dẹp.");
        }

        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        showtime.setStatus(ShowtimeStatus.UPCOMING);

        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại"));
        
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Phim không tồn tại"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Phòng không tồn tại"));

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new AppException("Suất chiếu không tồn tại");
        }
        showtimeRepository.deleteById(id);
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse response = new ShowtimeResponse();
        response.setId(showtime.getId());
        response.setMovieId(showtime.getMovie().getId());
        response.setMovieTitle(showtime.getMovie().getTitle());
        if (showtime.getRoom() != null) {
            response.setRoomId(showtime.getRoom().getId());
            response.setRoomName(showtime.getRoom().getName());
            if (showtime.getRoom().getType() != null) {
                response.setRoomType(showtime.getRoom().getType().name());
            }
        }
        response.setStartTime(showtime.getStartTime());
        response.setEndTime(showtime.getEndTime());
        response.setBasePrice(BigDecimal.valueOf(60000));
        response.setStatus(showtime.getStatus());
        return response;
    }
}
