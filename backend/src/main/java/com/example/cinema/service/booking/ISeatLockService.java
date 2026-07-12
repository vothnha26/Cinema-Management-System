package com.example.cinema.service.booking;

import java.util.List;

public interface ISeatLockService {
    void holdSeat(Long showtimeId, Long seatId, String sessionId);
    void releaseSeat(Long showtimeId, Long seatId, String sessionId);
    List<Long> getMyLockedSeats(Long showtimeId, String sessionId);
    void clearLocks(Long showtimeId, List<Long> seatIds);
    void broadcastStatus(Long showtimeId, Long seatId, String action, String sessionId);
}
