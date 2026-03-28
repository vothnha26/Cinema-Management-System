# 🧪 Testing – Cinema Management System

## 1. Chiến lược testing

```
Pyramid Testing:
         /\
        /E2E\         ← Không bắt buộc (dùng Postman thủ công)
       /──────\
      /  Integ  \     ← Integration Test (Controller + DB)
     /────────────\
    /  Unit Tests  \  ← Bắt buộc: tất cả Service (JUnit 5 + Mockito)
   /────────────────\
```

---

## 2. Unit Tests – Service Layer

**Công cụ**: JUnit 5 + Mockito

### Ví dụ: BookingServiceTest

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock private BookingRepository bookingRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private BookingDetailRepository bookingDetailRepository;
    @Mock private CustomerService customerService;
    @Mock private SeatPriceService seatPriceService;

    @Test
    @DisplayName("Đặt vé thành công khi ghế còn trống")
    void createBooking_success() {
        // Arrange
        Showtime showtime = new Showtime();
        showtime.setStatus(ShowtimeStatus.UPCOMING);
        when(showtimeRepository.findById(10L)).thenReturn(Optional.of(showtime));
        when(bookingDetailRepository.findBookedSeatIdsByShowtime(10L))
            .thenReturn(List.of());  // Không có ghế nào bị đặt

        BookingRequest request = new BookingRequest();
        request.setShowtimeId(10L);
        request.setSeatIds(List.of(101L, 102L));

        // Act
        BookingResponse response = bookingService.createBooking(request);

        // Assert
        assertNotNull(response.getBookingCode());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Ném exception khi ghế đã bị đặt")
    void createBooking_seatAlreadyBooked_throwsException() {
        // Arrange
        Showtime showtime = new Showtime();
        showtime.setStatus(ShowtimeStatus.UPCOMING);
        when(showtimeRepository.findById(10L)).thenReturn(Optional.of(showtime));
        when(bookingDetailRepository.findBookedSeatIdsByShowtime(10L))
            .thenReturn(List.of(101L));  // Ghế 101 đã bị đặt

        BookingRequest request = new BookingRequest();
        request.setShowtimeId(10L);
        request.setSeatIds(List.of(101L, 102L));  // Chọn ghế 101

        // Act & Assert
        assertThrows(AppException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném exception khi suất chiếu không tồn tại")
    void createBooking_showtimeNotFound_throwsException() {
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(999L);

        assertThrows(AppException.class, () -> bookingService.createBooking(request));
    }
}
```

---

## 3. Integration Tests – Controller Layer

**Công cụ**: Spring Boot Test + MockMvc + H2 (in-memory DB)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void postBooking_validRequest_returns201() throws Exception {
        BookingRequest request = new BookingRequest();
        // ... setup request

        mockMvc.perform(post("/api/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bookingCode").isNotEmpty());
    }

    @Test
    void postBooking_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## 4. Danh sách Test Cases tối thiểu

### BookingService
| Test case | Loại |
|-----------|------|
| Đặt vé thành công | Happy path |
| Ghế đã bị đặt → exception | Edge case |
| Suất chiếu không tồn tại → exception | Edge case |
| Suất chiếu không ở trạng thái UPCOMING → exception | Business rule |
| Promotion code không hợp lệ → exception | Business rule |
| Tính giá đúng với seat_prices | Calculation |

### CustomerService
| Test case | Loại |
|-----------|------|
| Tích điểm sau booking thành công | Happy path |
| Nâng hạng Silver khi đủ 500k | State change |
| Nâng hạng Gold khi đủ 2tr | State change |

### ShowtimeService
| Test case | Loại |
|-----------|------|
| Tạo suất chiếu thành công | Happy path |
| Xung đột lịch → exception | Conflict detection |

---

## 5. Chạy Test

```bash
# Chạy tất cả test
mvn test

# Chạy test cụ thể
mvn test -Dtest=BookingServiceTest

# Chạy với coverage report (JaCoCo)
mvn verify
# Xem report tại: target/site/jacoco/index.html
```

---

## 6. Test với Postman

Import collection để test API thủ công:
1. `POST /api/auth/login` → Lấy JWT token
2. Set token vào Authorization header
3. Chạy flow: Movies → Showtimes → Seats → Booking → Check-in
