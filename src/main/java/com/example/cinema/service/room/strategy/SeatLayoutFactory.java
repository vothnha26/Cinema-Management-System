package com.example.cinema.service.room.strategy;

import com.example.cinema.model.enums.RoomType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SeatLayoutFactory {
    private final Map<RoomType, SeatLayoutStrategy> strategies;
    private final StandardLayoutStrategy standardStrategy;

    public SeatLayoutFactory(
            StandardLayoutStrategy standardStrategy,
            ImaxLayoutStrategy imaxStrategy) {
        this.standardStrategy = standardStrategy;
        this.strategies = Map.of(
                RoomType.HALL_2D, standardStrategy,
                RoomType.HALL_3D, standardStrategy,
                RoomType.IMAX, imaxStrategy
        // Có thể thêm 4DX, LUXURY vào đây sau này
        );
    }

    public SeatLayoutStrategy getStrategy(RoomType type) {
        return strategies.getOrDefault(type, standardStrategy);
    }
}
