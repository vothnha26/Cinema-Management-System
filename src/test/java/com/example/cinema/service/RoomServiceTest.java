package com.example.cinema.service;

import com.example.cinema.model.dto.request.RoomRequest;
import com.example.cinema.model.dto.request.SeatBulkRequest;
import com.example.cinema.model.dto.request.SeatUpdateRequest;
import com.example.cinema.model.dto.response.RoomResponse;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Test
    public void testUpdateSeats() {
        // 1. Create a room
        RoomRequest roomReq = new RoomRequest();
        roomReq.setName("Test Room Service");
        roomReq.setType(RoomType.HALL_2D);
        roomReq.setRows(5);
        roomReq.setCols(5);
        RoomResponse room = roomService.createRoom(roomReq);
        Long roomId = room.getId();

        // 2. Prepare update
        SeatUpdateRequest seat1 = new SeatUpdateRequest("A", 1, SeatType.VIP, true);
        SeatUpdateRequest seat2 = new SeatUpdateRequest("A", 2, SeatType.VIP, true);
        
        SeatBulkRequest bulkReq = new SeatBulkRequest();
        bulkReq.setSeats(Arrays.asList(seat1, seat2));

        // 3. Update
        RoomResponse updatedRoom = roomService.updateSeats(roomId, bulkReq);

        // 4. Assert
        assertNotNull(updatedRoom);
        assertTrue(updatedRoom.getSeats().stream()
                .anyMatch(s -> s.getRowChar().equals("A") && s.getColNum() == 1 && s.getType() == SeatType.VIP));
        assertTrue(updatedRoom.getSeats().stream()
                .anyMatch(s -> s.getRowChar().equals("A") && s.getColNum() == 2 && s.getType() == SeatType.VIP));
    }
}
