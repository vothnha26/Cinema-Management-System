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
                .map(s -> {
                    ShowtimeResponse res = modelMapper.map(s, ShowtimeResponse.class);
                    res.setMovieTitle(s.getMovie().getTitle());
                    res.setRoomName(s.getRoom().getName());
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Không tìm thấy phim"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException("Không tìm thấy phòng"));

        // 1. Tính toán endTime dựa trên thời lượng phim + 15p dọn dẹp
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + 15);

        // 2. Kiểm tra xung đột phòng chiếu bằng Java
        LocalDateTime startOfDay = startTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startTime.toLocalDate().atTime(23, 59, 59);
        
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomAndDate(room.getId(), startOfDay, endOfDay);

        for (Showtime s : existingShowtimes) {
            // Công thức chồng chéo: start1 < end2 AND start2 < end1
            if (startTime.isBefore(s.getEndTime()) && endTime.isAfter(s.getStartTime())) {
                throw new AppException("Xung đột lịch chiếu: Phòng " + room.getName() + 
                    " đã có suất chiếu từ " + s.getStartTime().toLocalTime() + " đến " + s.getEndTime().toLocalTime());
            }
        }

        // 3. Tạo mới
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setStatus(ShowtimeStatus.UPCOMING);

        Showtime saved = showtimeRepository.save(showtime);
        
        ShowtimeResponse response = modelMapper.map(saved, ShowtimeResponse.class);
        response.setMovieTitle(movie.getTitle());
        response.setRoomName(room.getName());
        return response;
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
