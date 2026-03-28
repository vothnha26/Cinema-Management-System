package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.MovieRepository;
import com.example.cinema.repository.RoomRepository;
import com.example.cinema.repository.ShowtimeRepository;
import com.example.cinema.service.ShowtimeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private static final int CLEANING_TIME_MINUTES = 15;

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, 
                                MovieRepository movieRepository, 
                                RoomRepository roomRepository, 
                                ModelMapper modelMapper) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ShowtimeResponse> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(s -> modelMapper.map(s, ShowtimeResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Không tìm thấy phim"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Không tìm thấy phòng"));

        // 1. Tính toán endTime dựa trên thời lượng phim + dọn dẹp
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + CLEANING_TIME_MINUTES);

        // 2. Kiểm tra xung đột phòng chiếu
        validateShowtimeConflict(room.getId(), startTime, endTime);

        // 3. Tạo mới
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setStatus(ShowtimeStatus.UPCOMING);

        Showtime saved = showtimeRepository.save(showtime);
        return modelMapper.map(saved, ShowtimeResponse.class);
    }

    private void validateShowtimeConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime startOfDay = startTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startTime.toLocalDate().atTime(23, 59, 59);
        
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomAndDate(roomId, startOfDay, endOfDay);

        for (Showtime s : existingShowtimes) {
            if (startTime.isBefore(s.getEndTime()) && endTime.isAfter(s.getStartTime())) {
                throw new AppException("Xung đột lịch chiếu: Phòng đã có suất chiếu từ " + 
                    s.getStartTime().toLocalTime() + " đến " + s.getEndTime().toLocalTime());
            }
        }
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new AppException("Không tìm thấy suất chiếu");
        }
        showtimeRepository.deleteById(id);
    }
}
