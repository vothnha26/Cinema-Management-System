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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoomLayoutIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Test
    public void testFullRoomDesignFlow() {
        // 1. Tạo phòng mới 3x3
        RoomRequest roomReq = new RoomRequest();
        roomReq.setName("Integration Test Room");
        roomReq.setType(RoomType.HALL_2D);
        roomReq.setRows(3);
        roomReq.setCols(3);
        RoomResponse room = roomService.createRoom(roomReq);
        Long roomId = room.getId();

        // 2. Giả lập việc thay đổi sơ đồ từ UI:
        // - Chuyển hàng B thành VIP
        // - Ghế C3 bị hỏng (DISABLED)
        List<SeatUpdateRequest> updateRequests = new ArrayList<>();
        
        // Lấy danh sách ghế hiện tại để chuẩn bị request (giống UI design-room.html)
        for (RoomResponse.SeatResponse seat : room.getSeats()) {
            SeatUpdateRequest req = new SeatUpdateRequest();
            req.setRowChar(seat.getRowChar());
            req.setColNum(seat.getColNum());
            
            if (seat.getRowChar().equals("B")) {
                req.setType(SeatType.VIP);
                req.setStatus(true);
            } else if (seat.getRowChar().equals("C") && seat.getColNum() == 3) {
                req.setType(SeatType.DISABLED); // Tương ứng logic UI set DISABLED
                req.setStatus(false);
            } else {
                req.setType(seat.getType());
                req.setStatus(seat.getStatus());
            }
            updateRequests.add(req);
        }

        SeatBulkRequest bulkReq = new SeatBulkRequest();
        bulkReq.setSeats(updateRequests);

        // 3. Thực hiện cập nhật
        RoomResponse updatedRoom = roomService.updateSeats(roomId, bulkReq);

        // 4. Kiểm tra kết quả
        assertNotNull(updatedRoom);
        assertEquals(9, updatedRoom.getSeats().size());

        // Kiểm tra hàng B là VIP
        long vipCount = updatedRoom.getSeats().stream()
                .filter(s -> s.getRowChar().equals("B") && s.getType() == SeatType.VIP)
                .count();
        assertEquals(3, vipCount);

        // Kiểm tra ghế C3 là DISABLED và status false
        boolean isC3Disabled = updatedRoom.getSeats().stream()
                .anyMatch(s -> s.getRowChar().equals("C") && s.getColNum() == 3 
                        && s.getType() == SeatType.DISABLED && !s.getStatus());
        assertTrue(isC3Disabled);
    }
}
