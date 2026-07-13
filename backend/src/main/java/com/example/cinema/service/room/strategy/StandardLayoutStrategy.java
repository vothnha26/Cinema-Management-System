package com.example.cinema.service.room.strategy;

import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StandardLayoutStrategy implements SeatLayoutStrategy {
    @Override
    public List<Seat> generateSeats(Room room, int rows, int cols) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            String rowChar = String.valueOf((char) ('A' + i));
            for (int j = 1; j <= cols; j++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setRowChar(rowChar);
                seat.setColNum(j);

                // Mặc định VIP ở hàng 5-6, Couple ở cuối
                if (i >= 5 && i <= 6) {
                    seat.setType("VIP");
                } else if (i == rows - 1) {
                    seat.setType("COUPLE");
                } else {
                    seat.setType("STANDARD");
                }

                seat.setStatus(com.example.cinema.model.enums.SeatStatus.ACTIVE);
                seats.add(seat);
            }
        }
        return seats;
    }
}
