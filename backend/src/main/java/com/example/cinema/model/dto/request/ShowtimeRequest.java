package com.example.cinema.model.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ShowtimeRequest {
    @NotNull(message = "Movie ID không được để trống")
    private Long movieId;

    @NotNull(message = "Room ID không được để trống")
    private Long roomId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    private Long formatId;

    public ShowtimeRequest() {
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }
}
