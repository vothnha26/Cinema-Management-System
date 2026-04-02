package com.example.cinema.service.ticket.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.repository.booking.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * State Pattern:
 * Quản lý việc chuyển đổi trạng thái (State Transition) của vé.
 * Mỗi trạng thái chỉ được chuyển sang một tập trạng thái hợp lệ.
 *
 * PENDING    → CONFIRMED, CANCELLED
 * CONFIRMED  → CHECKED_IN, CANCELLED
 * CHECKED_IN → (final state)
 * CANCELLED  → (final state)
 *
 * SRP: Chỉ chịu trách nhiệm quản lý state transitions.
 * OCP: Thêm trạng thái mới chỉ cần sửa map VALID_TRANSITIONS.
 */
@Service
public class TicketWorkflowManager {

    private final BookingRepository bookingRepository;

    /**
     * Bảng chuyển trạng thái hợp lệ (State Transition Map).
     * Key: Trạng thái hiện tại → Value: Tập trạng thái có thể chuyển.
     */
    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(BookingStatus.class);
        VALID_TRANSITIONS.put(BookingStatus.PENDING,
                Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));
        VALID_TRANSITIONS.put(BookingStatus.CONFIRMED,
                Set.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED));
        VALID_TRANSITIONS.put(BookingStatus.CHECKED_IN,
                Set.of()); // final state
        VALID_TRANSITIONS.put(BookingStatus.CANCELLED,
                Set.of()); // final state
    }

    public TicketWorkflowManager(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Tra cứu vé bằng booking code. Dùng cho Staff tại quầy check-in.
     */
    public BookingResponse lookupByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "mã", bookingCode));
        return toResponse(booking);
    }

    /**
     * Check-in vé: Chuyển trạng thái từ CONFIRMED → CHECKED_IN.
     * Validate state transition trước khi chuyển.
     */
    @Transactional
    public BookingResponse checkin(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "mã", bookingCode));

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus targetStatus = BookingStatus.CHECKED_IN;

        // Validate state transition
        validateTransition(currentStatus, targetStatus, bookingCode);

        booking.setStatus(targetStatus);
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    /**
     * Lấy lịch sử tất cả các booking để hiển thị cho Staff.
     */
    public List<BookingResponse> getAllHistory() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tra cứu vé bằng ID.
     */
    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "ID", id.toString()));
        return toResponse(booking);
    }

    /**
     * Validate/Check-in vé bằng ID.
     */
    @Transactional
    public BookingResponse validateById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "ID", id.toString()));

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus targetStatus = BookingStatus.CHECKED_IN;

        validateTransition(currentStatus, targetStatus, booking.getBookingCode());

        booking.setStatus(targetStatus);
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    /**
     * Hủy vé: Chuyển trạng thái sang CANCELLED.
     */
    @Transactional
    public BookingResponse cancel(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "mã", bookingCode));

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus targetStatus = BookingStatus.CANCELLED;

        validateTransition(currentStatus, targetStatus, bookingCode);

        booking.setStatus(targetStatus);
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    /**
     * Kiểm tra tính hợp lệ của chuyển đổi trạng thái.
     */
    private void validateTransition(BookingStatus from, BookingStatus to, String bookingCode) {
        Set<BookingStatus> allowedStates = VALID_TRANSITIONS.getOrDefault(from, Set.of());

        if (!allowedStates.contains(to)) {
            String message;
            if (from == BookingStatus.CHECKED_IN) {
                message = "Vé " + bookingCode + " đã được check-in trước đó.";
            } else if (from == BookingStatus.CANCELLED) {
                message = "Vé " + bookingCode + " đã bị hủy, không thể thao tác.";
            } else if (from == BookingStatus.PENDING) {
                message = "Vé " + bookingCode + " đang chờ thanh toán, chưa thể check-in.";
            } else {
                message = "Không thể chuyển vé " + bookingCode + " từ " + from + " sang " + to + ".";
            }
            throw new AppException(message, 400);
        }
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(booking.getBookingCode());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus().name());

        if (booking.getShowtime() != null) {
            response.setMovieTitle(booking.getShowtime().getMovie().getTitle());
            response.setRoomName(booking.getShowtime().getRoom().getName());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            response.setShowtime(booking.getShowtime().getStartTime().format(fmt));
        }

        if (booking.getDetails() != null) {
            List<String> seatNames = booking.getDetails().stream()
                    .map(d -> d.getSeat().getRowChar() + d.getSeat().getColNum())
                    .collect(Collectors.toList());
            response.setSeats(seatNames);
        }

        if (booking.getCustomer() != null) {
            response.setCustomerName(booking.getCustomer().getFullName());
        }

        if (booking.getPayment() != null) {
            response.setPaymentMethod(booking.getPayment().getPaymentMethod().name());
        }

        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}
