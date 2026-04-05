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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final com.example.cinema.repository.movie.FormatRepository formatRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, 
                                SeatRepository seatRepository,
                                BookingDetailRepository bookingDetailRepository, 
                                SeatPriceRepository seatPriceRepository,
                                MovieRepository movieRepository,
                                RoomRepository roomRepository,
                                com.example.cinema.repository.movie.FormatRepository formatRepository,
                                StringRedisTemplate redisTemplate) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.seatPriceRepository = seatPriceRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.formatRepository = formatRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ShowtimeResponse> getAllShowtimes(LocalDate date) {
        List<Showtime> showtimes;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            showtimes = showtimeRepository.findAllByStartTimeBetween(start, end);
        } else {
            showtimes = showtimeRepository.findAll();
        }
        return showtimes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatStatusForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại"));
        
        List<Seat> allSeats = seatRepository.findByRoomId(showtime.getRoom().getId());
        
        // 1. Lấy ghế đã mua từ DB
        Set<Long> bookedSeatIds = new HashSet<>(bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId));
        
        // 2. Lấy ghế đang khóa từ Redis
        String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        Set<Long> lockedSeatIds = new HashSet<>();
        if (keys != null) {
            for (String key : keys) {
                String[] parts = key.split(":");
                lockedSeatIds.add(Long.parseLong(parts[2]));
            }
        }

        return allSeats.stream().map(seat -> {
            SeatResponse res = new SeatResponse();
            res.setId(seat.getId());
            res.setRowChar(seat.getRowChar());
            res.setColNum(seat.getColNum());
            res.setSeatCode(seat.getRowChar() + seat.getColNum());
            res.setSeatType(seat.getType());
            
            // Khả dụng nếu KHÔNG nằm trong cả 2 danh sách
            res.setAvailable(!bookedSeatIds.contains(seat.getId()) && !lockedSeatIds.contains(seat.getId()));
            
            BigDecimal price = seatPriceRepository.findLatestPrice(
                    showtime.getRoom().getType(), seat.getType(), showtime.getStartTime().toLocalDate())
                    .map(SeatPrice::getPrice)
                    .orElse(BigDecimal.valueOf(60000));
            res.setPrice(price);
            
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new AppException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("Room not found"));
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        showtime.setStatus(ShowtimeStatus.UPCOMING);
        showtime.setTotalSeats(room.getCapacity());
        showtime.setSoldSeats(0);
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Not found"));
        Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new AppException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("Room not found"));
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        Showtime s = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Not found"));
        if (s.getSoldSeats() > 0) throw new AppException("Suất chiếu đã bán vé");
        if (s.getStartTime().isBefore(LocalDateTime.now().plusHours(2))) throw new AppException("Cách giờ chiếu dưới 2 tiếng");
        showtimeRepository.deleteById(id);
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse res = new ShowtimeResponse();
        res.setId(showtime.getId());
        if (showtime.getMovie() != null) {
            Movie m = showtime.getMovie();
            res.setMovieId(m.getId());
            res.setMovieTitle(m.getTitle());
            res.setMovieDuration(m.getDuration());
            res.setPosterUrl(m.getPosterUrl());
            res.setAgeRating(m.getAgeRating() != null ? m.getAgeRating().name() : "P");
            res.setGenres(m.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
        }
        if (showtime.getRoom() != null) {
            res.setRoomId(showtime.getRoom().getId());
            res.setRoomName(showtime.getRoom().getName());
            res.setRoomType(showtime.getRoom().getType().name());
        }
        res.setStartTime(showtime.getStartTime());
        res.setEndTime(showtime.getEndTime());
        if (showtime.getFormat() != null) res.setFormatName(showtime.getFormat().getName());
        res.setStatus(showtime.getStatus());
        res.setTotalSeats(showtime.getTotalSeats());
        res.setSoldSeats(showtime.getSoldSeats());
        return res;
    }
}
