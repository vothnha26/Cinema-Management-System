package com.example.cinema.service.booking;

import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse finalizeBooking(String bookingCode, String transactionId);
    
    void holdSeat(Long showtimeId, Long seatId, String sessionId);
    void releaseSeat(Long showtimeId, Long seatId, String sessionId);
    List<Long> getMyLockedSeats(Long showtimeId, String sessionId);

    List<BookingResponse> getMyBookings();
    BookingResponse getMyBookingByCode(String bookingCode);
    void cancelBooking(String bookingCode);
    void checkInBooking(String bookingCode);
}
