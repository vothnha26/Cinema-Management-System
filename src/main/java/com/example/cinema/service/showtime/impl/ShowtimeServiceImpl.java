package com.example.cinema.service.showtime.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.showtime.ShowtimeService;
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
    @LogAction(action = "CREATE", target = "SHOWTIME")
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Không tìm thấy phim"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Không tìm thấy phòng"));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + CLEANING_TIME_MINUTES);

        validateShowtimeConflict(null, room.getId(), startTime, endTime);

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setStatus(ShowtimeStatus.UPCOMING);

        Showtime saved = showtimeRepository.save(showtime);
        return modelMapper.map(saved, ShowtimeResponse.class);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "SHOWTIME")
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy suất chiếu"));
        
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Không tìm thấy phim"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Không tìm thấy phòng"));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + CLEANING_TIME_MINUTES);

        // Kiểm tra xung đột, loại trừ chính nó
        validateShowtimeConflict(id, room.getId(), startTime, endTime);

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);

        Showtime updated = showtimeRepository.save(showtime);
        return modelMapper.map(updated, ShowtimeResponse.class);
    }

    private void validateShowtimeConflict(Long excludeId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime startOfDay = startTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startTime.toLocalDate().atTime(23, 59, 59);
        
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomAndDate(roomId, startOfDay, endOfDay);

        for (Showtime s : existingShowtimes) {
            // Bỏ qua nếu là chính suất chiếu đang cập nhật
            if (excludeId != null && s.getId().equals(excludeId)) continue;

            if (startTime.isBefore(s.getEndTime()) && endTime.isAfter(s.getStartTime())) {
                throw new AppException("Xung đột lịch chiếu: Phòng đã có suất chiếu từ " + 
                    s.getStartTime().toLocalTime() + " đến " + s.getEndTime().toLocalTime());
            }
        }
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "SHOWTIME")
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new AppException("Không tìm thấy suất chiếu");
        }
        showtimeRepository.deleteById(id);
    }
}
