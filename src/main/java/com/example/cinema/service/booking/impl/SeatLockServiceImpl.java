package com.example.cinema.service.booking.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.constant.AppConstants;
import com.example.cinema.service.booking.ISeatLockService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatLockServiceImpl implements ISeatLockService {
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String LOCK_KEY_PREFIX = AppConstants.REDIS_SEAT_LOCK_PREFIX;
    private static final String DEADLINE_KEY_PREFIX = AppConstants.REDIS_SEAT_DEADLINE_PREFIX;
    private static final Duration HOLD_DURATION = Duration.ofMinutes(AppConstants.REDIS_HOLD_DURATION_MINUTES);

    public SeatLockServiceImpl(StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void holdSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        String currentLockOwner = redisTemplate.opsForValue().get(lockKey);

        if (currentLockOwner == null) {
            redisTemplate.opsForValue().set(lockKey, sessionId, HOLD_DURATION);
            redisTemplate.opsForValue().set(deadlineKey, "ACTIVE", HOLD_DURATION);
            broadcastStatus(showtimeId, seatId, "HOLD", sessionId);
        } else if (sessionId.equals(currentLockOwner)) {
            redisTemplate.expire(lockKey, HOLD_DURATION);
            redisTemplate.expire(deadlineKey, HOLD_DURATION);
        } else {
            throw new AppException("Ghế này đang được người khác chọn");
        }
    }

    @Override
    public void releaseSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        if (sessionId.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey);
            redisTemplate.delete(deadlineKey);
            broadcastStatus(showtimeId, seatId, "RELEASE", sessionId);
        }
    }

    @Override
    public List<Long> getMyLockedSeats(Long showtimeId, String sessionId) {
        String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return new ArrayList<>();
        return keys.stream().filter(k -> sessionId.equals(redisTemplate.opsForValue().get(k)))
                .map(k -> {
                    String[] parts = k.split(":");
                    return Long.parseLong(parts[parts.length - 1]);
                }).collect(Collectors.toList());
    }

    @Override
    public void clearLocks(Long showtimeId, List<Long> seatIds) {
        for (Long sId : seatIds) {
            redisTemplate.delete(LOCK_KEY_PREFIX + showtimeId + ":" + sId);
            broadcastStatus(showtimeId, sId, "BOOKED", "SYSTEM-AUTO");
        }
    }

    @Override
    public void broadcastStatus(Long showtimeId, Long seatId, String action, String sessionId) {
        Map<String, Object> message = new HashMap<>();
        message.put("seatId", seatId);
        message.put("action", action);
        message.put("sessionId", sessionId);
        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId + "/seats", message);
    }
}
