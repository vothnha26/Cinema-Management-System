package com.example.cinema.service.room.strategy;

import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Seat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImaxLayoutStrategy implements SeatLayoutStrategy {
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

                // IMAX: Khu vực trung tâm (hàng 4 đến hàng 10) toàn bộ là VIP
                if (i >= 4 && i <= 10) {
                    seat.setType("VIP");
                } else if (i == rows - 1) {
                    seat.setType("COUPLE");
                } else {
                    seat.setType("STANDARD");
                }

                seat.setStatus(true);
                seats.add(seat);
            }
        }
        return seats;
    }
}
