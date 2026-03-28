package com.example.cinema.service.strategy;

import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import java.util.List;

public interface SeatLayoutStrategy {
    List<Seat> generateSeats(Room room, int rows, int cols);
}
