package com.example.cinema.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public class BulkShowtimeRequest {
    private Long movieId;
    private Long roomId;
    private Long formatId;
    private List<LocalDateTime> startTimes;

    // Getters and Setters
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getFormatId() { return formatId; }
    public void setFormatId(Long formatId) { this.formatId = formatId; }
    public List<LocalDateTime> getStartTimes() { return startTimes; }
    public void setStartTimes(List<LocalDateTime> startTimes) { this.startTimes = startTimes; }
}
