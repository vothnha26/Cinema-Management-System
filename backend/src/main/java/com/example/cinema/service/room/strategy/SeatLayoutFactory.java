package com.example.cinema.service.room.strategy;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class SeatLayoutFactory {

    private final Map<String, SeatLayoutStrategy> strategies;

    public SeatLayoutFactory(Map<String, SeatLayoutStrategy> strategies) {
        this.strategies = strategies;
    }

    public SeatLayoutStrategy getStrategy(String roomTypeId) {
        if (roomTypeId == null) {
            return strategies.get("standardLayoutStrategy");
        }

        switch (roomTypeId) {
            case "IMAX":
                return strategies.get("imaxLayoutStrategy");
            default:
                return strategies.get("standardLayoutStrategy");
        }
    }
}
