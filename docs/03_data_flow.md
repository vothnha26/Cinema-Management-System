# 🔄 Flow xử lý – Data Flow / Business Flow

## 1. Request Flow tổng quát

```
Client (Browser)
  │
  │  HTTP Request (GET/POST/PUT/DELETE)
  ▼
JwtFilter (Spring Security)
  │  Xác thực token → Lấy UserDetails → Set SecurityContext
  ▼
@RestController
  │  Nhận request → @Valid DTO → Gọi Service
  ▼
Service (Interface)
  │  Xử lý nghiệp vụ → Gọi Repository
  ▼
Repository (JpaRepository)
  │  Truy vấn CSDL qua Hibernate
  ▼
MySQL Database
  │  Trả kết quả
  ▼
Service → Entity → DTO (ModelMapper)
  ▼
Controller → ResponseEntity<ApiResponse<DTO>>
  ▼
Client nhận JSON response
```

---

## 2. Flow đặt vé online (Luồng quan trọng nhất)

```
CUSTOMER gửi POST /api/bookings
{
  "showtimeId": 10,
  "seatIds": [101, 102],
  "comboOrders": [{"comboId": 1, "quantity": 2}],
  "promotionCode": "GOLD10",
  "paymentMethod": "MOMO"
}

─────────────────────────────────────────────
STEP 1: BookingController
  ✅ @Valid BookingRequest (kiểm tra ràng buộc @NotNull, @NotEmpty)
  ✅ Gọi bookingService.createBooking(request)

─────────────────────────────────────────────
STEP 2: BookingService.createBooking()
  ✅ [Kiểm tra 1] Suất chiếu tồn tại & status = UPCOMING
     → ShowtimeRepository.findById()
     → Throw AppException nếu không hợp lệ

  ✅ [Kiểm tra 2] Ghế chưa bị đặt trong suất chiếu này
     → BookingDetailRepository.findBookedSeatIdsByShowtime()
     → Throw AppException nếu ghế đã bị đặt

  ✅ [Kiểm tra 3] Áp promotion code (nếu có)
     → PromotionRepository.findByCode()
     → Kiểm tra hạn sử dụng, hạng thành viên tối thiểu

  ✅ [Tính giá]
     price = Σ(seat_price) + Σ(combo_price × qty) − discount

  ✅ [Lưu DB - @Transactional]
     → Tạo Booking entity (booking_code = UUID)
     → Tạo List<BookingDetail> (1 record / ghế)
     → Tạo List<BookingCombo> (1 record / combo)
     → BookingRepository.save(booking) [cascade ALL]

─────────────────────────────────────────────
STEP 3: PaymentService.processPayment()
  ✅ Tạo Payment entity
  ✅ Gọi payment gateway (mock / thật)
  ✅ Cập nhật payment_status = SUCCESS / FAILED

─────────────────────────────────────────────
STEP 4: CustomerService.rewardPoints()
  ✅ Cộng điểm tích lũy = total_price / 10000
  ✅ Cập nhật total_spent
  ✅ Kiểm tra nâng hạng thành viên tự động

─────────────────────────────────────────────
STEP 5: NotificationService.sendBookingConfirmation()
  ✅ Tạo Notification entity (type = BOOKING)
  ✅ [Tùy chọn] Gửi email xác nhận

─────────────────────────────────────────────
STEP 6: Response
  → BookingResponse {
      bookingCode, totalPrice,
      seatCodes, showtimeInfo,
      paymentStatus, pointsEarned
    }
```

---

## 3. Flow bán vé tại quầy (Staff - POS)

```
STAFF → Tìm suất chiếu theo ngày/phim
       → Xem sơ đồ ghế còn trống (GET /api/showtimes/{id}/seats)
       → Chọn ghế
       → POST /api/bookings (paymentMethod = CASH)
       → In vé / hiển thị booking_code
```

## 4. Flow Check-in vé

```
STAFF nhập / quét booking_code
  → PUT /api/bookings/{code}/checkin
  → BookingService.checkIn()
     ✅ Kiểm tra booking tồn tại & status = CONFIRMED
     ✅ Kiểm tra đúng suất chiếu & ngày hôm nay
     ✅ Cập nhật status = CHECKED_IN
  → Trả xác nhận check-in thành công
```

---

## 5. Xử lý lỗi tập trung

```
Bất kỳ tầng nào throw AppException
  → GlobalExceptionHandler (@ControllerAdvice)
      @ExceptionHandler(AppException.class)
      → Trả JSON: { "success": false, "message": "...", "code": 400 }
```
