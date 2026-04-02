package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.BookingDetailRepository;
import com.example.cinema.repository.SeatPriceRepository;
import com.example.cinema.repository.SeatRepository;
import com.example.cinema.repository.ShowtimeRepository;
import com.example.cinema.service.ShowtimeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SeatPriceRepository seatPriceRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, SeatRepository seatRepository,
                               BookingDetailRepository bookingDetailRepository, SeatPriceRepository seatPriceRepository) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.seatPriceRepository = seatPriceRepository;
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
        
        // Find booked seats for this showtime
        List<Long> bookedSeatIds = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);

        return seats.stream().map(seat -> {
            SeatResponse response = new SeatResponse();
            response.setId(seat.getId());
            response.setSeatCode(seat.getSeatCode());
            response.setSeatType(seat.getType());
            response.setRowChar(seat.getRowChar());
            response.setColNum(seat.getColNum());
            
            // Check availability
            response.setAvailable(!bookedSeatIds.contains(seat.getId()));

            // Find price based on room type and seat type
            SeatPrice seatPrice = seatPriceRepository.findLatestPrice(
                    room.getType(), seat.getType(), java.time.LocalDate.now()).orElse(null);
            
            BigDecimal price = BigDecimal.valueOf(seat.getType().name().equals("VIP") ? 80000 : 60000);
            if (seatPrice != null) {
                price = seatPrice.getPrice(); // Simplified price calculation
            }
            response.setPrice(price);
            
            return response;
        }).collect(Collectors.toList());
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
        response.setBasePrice(BigDecimal.valueOf(60000)); // Default base value
        response.setStatus(showtime.getStatus());
        return response;
    }
}
