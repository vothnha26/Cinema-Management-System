# 🧠 Business Logic (BLL) – Cinema Management System

## 1. Logic nằm ở đâu?

```
Controller    ❌ KHÔNG chứa logic nghiệp vụ
Service       ✅ TẤT CẢ logic nghiệp vụ nằm ở đây
Repository    ❌ Chỉ truy vấn DB (có thể chứa @Query phức tạp)
Entity        ❌ Chỉ là data model (không có hành vi nghiệp vụ)
```

---

## 2. Các Service & Logic chính

### 2.1. BookingService – Logic đặt vé

```java
// Rule 1: Kiểm tra suất chiếu hợp lệ
if (showtime.getStatus() != ShowtimeStatus.UPCOMING)
    throw new AppException("Suất chiếu không khả dụng");

// Rule 2: Kiểm tra ghế chưa bị đặt
List<Long> bookedSeats = bookingDetailRepo.findBookedSeatIdsByShowtime(showtimeId);
if (request.getSeatIds().stream().anyMatch(bookedSeats::contains))
    throw new AppException("Ghế đã được đặt, vui lòng chọn ghế khác");

// Rule 3: Tính giá vé theo SeatPrice
BigDecimal seatTotal = seats.stream()
    .map(seat -> seatPriceService.getPrice(room.getRoomType(), seat.getSeatType()))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Rule 4: Tính giá combo
BigDecimal comboTotal = comboOrders.stream()
    .map(o -> o.getCombo().getPrice().multiply(BigDecimal.valueOf(o.getQuantity())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Rule 5: Áp ưu đãi (promotion code hoặc membership discount)
BigDecimal discount = discountStrategy.calculate(customerId, promotionCode, subtotal);
BigDecimal total = seatTotal.add(comboTotal).subtract(discount);
```

### 2.2. SeatPriceService – Logic tính giá vé

```
Giá vé = f(loại phòng, loại ghế)

Bảng seat_prices:
  IMAX + VIP      = 180,000đ
  3D   + STANDARD = 95,000đ
  2D   + COUPLE   = 160,000đ
  ...

Ưu tiên: Lấy bản ghi is_active=true & effective_date gần nhất
```

### 2.3. CustomerService – Logic thành viên

```
// Tích điểm: 1 điểm / 10,000đ chi tiêu
int points = (int)(totalPrice / 10_000);
customer.setPoints(customer.getPoints() + points);
customer.setTotalSpent(customer.getTotalSpent().add(totalPrice));

// Nâng hạng tự động
if (totalSpent >= 5_000_000)       tier = PLATINUM  // -15%
else if (totalSpent >= 2_000_000)  tier = GOLD      // -10%
else if (totalSpent >= 500_000)    tier = SILVER    // -5%
else                               tier = STANDARD
```

### 2.4. PromotionService – Logic khuyến mãi

```
Validation:
  ✅ promotion.isActive() = true
  ✅ Ngày hiện tại trong [start_date, end_date]
  ✅ Hạng thành viên >= min_tier của promotion
  ✅ Chưa từng dùng code này (tùy chọn)

Tính discount:
  PERCENT: discount = price * discount_value / 100
  FIXED:   discount = discount_value
```

### 2.5. ShowtimeService – Kiểm tra xung đột lịch

```java
// Không cho tạo 2 suất chiếu cùng phòng, trùng thời gian
boolean conflict = showtimeRepo
    .existsByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
        roomId, endTime, startTime
    );
if (conflict) throw new AppException("Phòng đã có suất chiếu trong khung giờ này");
```

---

## 3. Validation Rules

| Field | Rule |
|-------|------|
| `Booking.seatIds` | `@NotEmpty`, size ≥ 1, ≤ 8 ghế / lần đặt |
| `Showtime.startTime` | Phải là tương lai, cách hiện tại ít nhất 30 phút |
| `Movie.ageRating` | Enum: P, C13, C16, C18 |
| `User.password` | ≥ 8 ký tự, có chữ hoa và số |
| `Promo.discountValue` | PERCENT: 1–100; FIXED: > 0 |

---

## 4. Discount Strategy (SOLID - Open/Closed)

```java
interface DiscountStrategy {
    BigDecimal calculate(BigDecimal price, Object context);
}

// Các implementation:
class MembershipDiscount  implements DiscountStrategy { ... }
class PromotionCodeDiscount implements DiscountStrategy { ... }
class BirthdayDiscount    implements DiscountStrategy { ... } // mở rộng thêm không sửa code cũ

// Kết hợp nhiều strategy (Chain):
BigDecimal finalPrice = strategies.stream()
    .reduce(price, (p, s) -> s.calculate(p, context), (a, b) -> b);
```

---

## 5. Transaction & Data Integrity

- `@Transactional` bọc toàn bộ quá trình tạo booking (Booking + Details + Combos + Payment).
- Nếu payment thất bại → rollback toàn bộ → ghế không bị giữ.
- `PESSIMISTIC_WRITE` lock trên bảng `booking_details` để tránh 2 user chọn cùng ghế cùng lúc.
