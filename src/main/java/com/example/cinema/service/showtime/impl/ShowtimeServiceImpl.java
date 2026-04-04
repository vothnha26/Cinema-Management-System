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

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, 
                                SeatRepository seatRepository,
                                BookingDetailRepository bookingDetailRepository, 
                                SeatPriceRepository seatPriceRepository,
                                MovieRepository movieRepository,
                                RoomRepository roomRepository,
                                com.example.cinema.repository.movie.FormatRepository formatRepository) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.seatPriceRepository = seatPriceRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.formatRepository = formatRepository;
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
        Set<Long> bookedSeatIds = new HashSet<>(bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId));

        return allSeats.stream().map(seat -> {
            SeatResponse res = new SeatResponse();
            res.setId(seat.getId());
            res.setRowChar(seat.getRowChar());
            res.setColNum(seat.getColNum());
            res.setSeatCode(seat.getRowChar() + seat.getColNum());
            res.setSeatType(seat.getType());
            res.setAvailable(!bookedSeatIds.contains(seat.getId()));
            
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
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Movie not found"));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Room not found"));

        Format format = null;
        if (request.getFormatId() != null) {
            format = formatRepository.findById(request.getFormatId()).orElse(null);
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setFormat(format);
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
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy suất chiếu"));
        
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Movie not found"));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Room not found"));

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại"));

        if (showtime.getSoldSeats() > 0) {
            throw new AppException("Không thể xóa suất chiếu đã có khách đặt vé");
        }

        if (showtime.getStartTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new AppException("Chỉ có thể xóa suất chiếu trước giờ bắt đầu ít nhất 2 tiếng");
        }

        showtimeRepository.deleteById(id);
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse response = new ShowtimeResponse();
        response.setId(showtime.getId());
        
        if (showtime.getMovie() != null) {
            Movie m = showtime.getMovie();
            response.setMovieId(m.getId());
            response.setMovieTitle(m.getTitle());
            response.setMovieDuration(m.getDuration());
            response.setMovieRating(m.getRating());
            response.setPosterUrl(m.getPosterUrl());
            response.setAgeRating(m.getAgeRating() != null ? m.getAgeRating().name() : "P");
            response.setGenres(m.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
        }
        
        if (showtime.getRoom() != null) {
            response.setRoomId(showtime.getRoom().getId());
            response.setRoomName(showtime.getRoom().getName());
            response.setRoomType(showtime.getRoom().getType().name());
        }
        
        response.setStartTime(showtime.getStartTime());
        response.setEndTime(showtime.getEndTime());
        
        if (showtime.getFormat() != null) {
            response.setFormatName(showtime.getFormat().getName());
        }

        response.setBasePrice(BigDecimal.valueOf(60000));
        response.setStatus(showtime.getStatus());
        response.setTotalSeats(showtime.getTotalSeats());
        response.setSoldSeats(showtime.getSoldSeats());
        
        return response;
    }
}
