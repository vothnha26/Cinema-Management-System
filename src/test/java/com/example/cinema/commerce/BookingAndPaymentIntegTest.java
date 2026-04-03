package com.example.cinema.commerce;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.dto.request.OnlineBookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.booking.impl.CustomerBookingFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BookingAndPaymentIntegTest extends BaseIntegTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerBookingFacade bookingFacade;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("SLS-4.1 & 4.3: Test luồng Đặt vé Member -> Webhook Thanh toán thành công")
    void testBookingAndPaymentFlow() throws Exception {
        // 0. Chuẩn bị Dữ liệu (Movie, Room, Showtime, User, Customer)
        Movie movie = new Movie();
        movie.setTitle("Payment Test Movie"); movie.setDuration(120); movie.setStatus(MovieStatus.NOW_SHOWING);
        movie = movieRepository.save(movie);
        
        Room room = new Room();
        room.setName("Hall Payment"); room.setCapacity(50); room.setType(RoomType.HALL_2D);
        room = roomRepository.save(room);
        
        Showtime showtime = new Showtime();
        showtime.setMovie(movie); showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusHours(1));
        showtime.setEndTime(showtime.getStartTime().plusMinutes(movie.getDuration()));
        showtime.setStatus(ShowtimeStatus.UPCOMING);
        showtime = showtimeRepository.save(showtime);

        User user = new User();
        user.setUsername("member_vip"); user.setEmail("vip@example.com"); user.setPassword("123456"); user.setRole(Role.CUSTOMER);
        user = userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user); customer.setFullName("Member VIP");
        customer = customerRepository.save(customer);

        // 1. Tạo Đặt vé (Member)
        OnlineBookingRequest bookingRequest = new OnlineBookingRequest();
        bookingRequest.setShowtimeId(showtime.getId());
        bookingRequest.setSeatIds(List.of(1L, 2L)); 
        bookingRequest.setPaymentMethod("VNPAY");

        BookingResponse bookingResponse = bookingFacade.processOnlineBooking(bookingRequest, "member_vip");

        assertNotNull(bookingResponse.getBookingCode());
        // Lưu ý: Response có thể trả về CONFIRMED hoặc PENDING tùy logic Facade hiện tại
        // Nhưng trong DB phải là PENDING
        
        // 2. Mô phỏng SePay Webhook cập nhật trạng thái
        String bookingCode = bookingResponse.getBookingCode();
        String jsonPayload = String.format("""
            {
                "id": 123456,
                "content": "Thanh toan cho %s",
                "transferContent": "Thanh toan cho %s",
                "amount": 190000,
                "status": "SUCCESS"
            }
        """, bookingCode, bookingCode);

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        // 3. Kiểm tra DB xem Booking đã CONFIRMED chưa
        Booking updatedBooking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking sau webhook"));

        assertEquals(BookingStatus.CONFIRMED, updatedBooking.getStatus(), 
            "Booking phải được chuyển sang trạng thái CONFIRMED sau khi nhận Webhook thành công.");
    }
}
