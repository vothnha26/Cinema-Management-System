package com.example.cinema.controller.staff;

import com.example.cinema.model.dto.request.PosBookingRequest;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String staffToken;
    private Customer globalCustomer;
    private Showtime globalShowtime;

    @BeforeEach
    void setUp() {
        // Tạo tài khoản Staff mẫu để thực hiện hành động (Actor: Staff)
        User staff = new User();
        staff.setUsername("staff_tester_01");
        staff.setEmail("staff_tester@starcinema.vn");
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setRole(Role.STAFF);
        staff.setStatus(true);
        userRepository.save(staff);
        
        staffToken = "Bearer " + jwtUtil.generateToken(staff, Role.STAFF.name());

        // 1. Tạo Customer liên kết với User
        User userCust = new User();
        userCust.setUsername("cust_tester_01");
        userCust.setEmail("cust_tester@gmail.com");
        userCust.setPassword(passwordEncoder.encode("password123"));
        userCust.setRole(Role.CUSTOMER);
        userCust.setStatus(true);
        userRepository.save(userCust);

        globalCustomer = new Customer();
        globalCustomer.setUser(userCust);
        globalCustomer.setFullName("Customer Tester");
        globalCustomer.setPhone("0987654321");
        customerRepository.save(globalCustomer);

        // Chuẩn bị sẵn Movie và Room để tạo Showtime nếu cần
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setDuration(120);
        movie.setStatus(MovieStatus.NOW_SHOWING);
        movie.setAgeRating(AgeRating.P);
        movieRepository.save(movie);

        Room room = new Room();
        room.setName("Room 1");
        room.setType(RoomType.HALL_2D);
        room.setCapacity(50);
        roomRepository.save(room);

        globalShowtime = new Showtime();
        globalShowtime.setMovie(movie);
        globalShowtime.setRoom(room);
        globalShowtime.setStartTime(LocalDateTime.now().plusHours(1));
        globalShowtime.setEndTime(LocalDateTime.now().plusHours(3));
        globalShowtime.setStatus(ShowtimeStatus.UPCOMING);
        globalShowtime.setTotalSeats(50);
        showtimeRepository.save(globalShowtime);
    }

    @Test
    @DisplayName("Staff SD: Bán vé tại quầy (POS Booking) thành công")
    public void testPosBooking_Success() throws Exception {
        // Tạo Seat thực tế
        Seat seat1 = new Seat();
        seat1.setRoom(globalShowtime.getRoom());
        seat1.setRowChar("A");
        seat1.setColNum(1);
        seat1.setType(SeatType.STANDARD);
        seatRepository.save(seat1);

        Seat seat2 = new Seat();
        seat2.setRoom(globalShowtime.getRoom());
        seat2.setRowChar("A");
        seat2.setColNum(2);
        seat2.setType(SeatType.STANDARD);
        seatRepository.save(seat2);

        PosBookingRequest request = new PosBookingRequest();
        request.setShowtimeId(globalShowtime.getId());
        request.setSeatIds(List.of(seat1.getId(), seat2.getId()));
        request.setPaymentMethod("CASH");
        request.setCustomerPhone(globalCustomer.getPhone());

        mockMvc.perform(post("/api/staff/pos/book")
                .header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingCode").exists())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Staff SD: Soát vé (Ticket Validation) thành công")
    public void testValidateTicket_Success() throws Exception {
        // 1. Tạo một Booking đã thanh toán (PAID) để soát vé
        Booking booking = new Booking();
        booking.setCustomer(globalCustomer);
        booking.setShowtime(globalShowtime);
        booking.setBookingCode("TICKET-" + UUID.randomUUID().toString().substring(0, 5));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("190000"));
        bookingRepository.save(booking);

        // 2. Thực hiện soát vé theo Sequence Diagram
        mockMvc.perform(post("/api/staff/tickets/" + booking.getId() + "/validate")
                .header("Authorization", staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CHECKED_IN"));
    }

    @Test
    @DisplayName("Staff SD: Soát vé thất bại do vé đã sử dụng")
    public void testValidateTicket_AlreadyCheckedIn() throws Exception {
        Booking booking = new Booking();
        booking.setCustomer(globalCustomer);
        booking.setShowtime(globalShowtime);
        booking.setBookingCode("USED-TICKET");
        booking.setStatus(BookingStatus.CHECKED_IN); // Đã soát rồi
        booking.setTotalPrice(new BigDecimal("190000"));
        bookingRepository.save(booking);

        mockMvc.perform(post("/api/staff/tickets/" + booking.getId() + "/validate")
                .header("Authorization", staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Staff SD: Lấy lịch sử soát vé")
    public void testGetValidationHistory() throws Exception {
        mockMvc.perform(get("/api/staff/tickets/history")
                .header("Authorization", staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Security: Customer không thể truy cập API Staff POS")
    public void testStaffApi_ForbiddenForCustomer() throws Exception {
        User customer = new User();
        customer.setUsername("cust_01");
        customer.setEmail("cust01@gmail.com");
        customer.setPassword(passwordEncoder.encode("password123"));
        customer.setRole(Role.CUSTOMER);
        customer.setStatus(true);
        userRepository.save(customer);
        
        String customerToken = "Bearer " + jwtUtil.generateToken(customer, Role.CUSTOMER.name());

        mockMvc.perform(get("/api/staff/tickets/history")
                .header("Authorization", customerToken))
                .andExpect(status().isForbidden());
    }
}
