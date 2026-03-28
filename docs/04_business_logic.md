# 🧠 Business Logic (BLL) – Cinema Management System (Elite Edition)

## 1. Phân tầng & Trách nhiệm (SRP)

```
Controller    ❌ KHÔNG chứa logic nghiệp vụ
Service       ✅ TẤT CẢ logic nghiệp vụ nằm ở đây (Sử dụng Design Patterns)
Pricing       💎 Decorator Pattern: Tính giá vé cộng dồn linh hoạt
Scheduling    🤖 AI Algorithm: Tự động hóa xếp lịch theo xu hướng (TMDB)
Aspects       🛡️ Audit Log: Tự động lưu vết thao tác (AOP)
Repository    📦 Truy vấn dữ liệu, Native SQL thống kê nâng cao
```

---

## 2. Các Logic Elite tiêu biểu

### 2.1. Pricing Engine (Decorator Pattern)
Thay vì tính giá phẳng, hệ thống sử dụng cấu trúc lớp bọc (Layered Pricing):
- **BasePrice**: Lấy từ cấu hình `seat_prices`.
- **RoomTypeDecorator**: +50k cho IMAX, +80k cho 4DX.
- **SeatTypeDecorator**: +20k cho VIP, +40k cho COUPLE.
- **DayOfWeekDecorator**: -10k cho Thứ 2/3, +10k cho Cuối tuần.
- **TimeSlotDecorator**: -15k cho Happy Hour (trước 12h sáng).

### 2.2. AI Scheduling Algorithm (Task 9)
Thuật toán tự động gợi ý lịch chiếu dựa trên:
1. **Priority Score**: `(Manual Priority * 20) + (TMDB Popularity Score)`.
2. **70/30 Rule**: Dành 70% các slot "Giờ vàng" (Prime Time) cho các phim có điểm cao nhất.
3. **Prime Time Logic**:
    - Ngày thường: 17:00 - 22:00.
    - Cuối tuần: 10:00 - 23:00 (Mở rộng).
4. **Staggered Starts**: Giờ bắt đầu giữa các phòng lệch nhau 15-30p để tối ưu vận hành rạp.

### 2.3. Showtime Conflict Detection
Kiểm tra chính xác đến từng phút bằng logic Java:
- Một suất chiếu mới `(S_new, E_new)` xung đột nếu tồn tại suất chiếu `(S_old, E_old)` sao cho: `S_new < E_old AND E_new > S_old`.
- `E_old` luôn bao gồm **15 phút dọn dẹp** sau phim.

### 2.4. Promotion Engine (Builder Pattern)
Sử dụng **Builder** để tạo chương trình khuyến mãi với các điều kiện tùy chọn:
- `minOrderAmount`: Đơn hàng tối thiểu.
- `maxDiscountAmount`: Giới hạn giảm tối đa cho loại PERCENT.
- `usageLimit`: Tổng lượt sử dụng toàn hệ thống.
- `minTier`: Hạng thành viên tối thiểu (Standard, Silver, Gold, Diamond).

---

## 3. Hệ thống Nhật ký & Kiểm toán (Audit Log)

Sử dụng **Spring AOP** để tự động hóa việc giám sát Manager:
- **Annotation `@LogAction`**: Đánh dấu các phương thức nhạy cảm (CREATE_MOVIE, DELETE_ROOM...).
- **AuditLogAspect**: Interceptor tự động lấy thông tin `Username` từ SecurityContext, `Action`, `Target` và `Timestamp`.
- **Async Logging**: Việc ghi log không làm chậm phản hồi của API chính.

---

## 4. Quy tắc chuẩn hóa dữ liệu (Validation)

| Đối tượng | Quy tắc (Elite Standard) |
|-----------|--------------------------|
| **Phim** | Nhãn độ tuổi chuẩn: P, K, T13, T16, T18. |
| **Phòng** | Tự động sinh sơ đồ ghế theo Strategy (IMAX, 2D, 4DX). |
| **Suất chiếu** | Không cho phép cập nhật nếu suất chiếu đã bắt đầu hoặc kết thúc. |
| **Bảng giá** | Tự động vô hiệu hóa cấu hình cũ khi cập nhật giá mới cho cùng loại phòng/ghế. |

---

## 5. Thống kê nâng cao (Native SQL)
- **Doanh thu thực tế**: Chỉ tính các Booking ở trạng thái `CONFIRMED` hoặc `CHECKED_IN`.
- **Tỷ trọng phim**: Tính % đóng góp doanh thu của từng phim trong tổng doanh thu rạp.
- **Biểu đồ 7 ngày**: Truy vấn theo Group By DATE(created_at) để vẽ chuỗi thời gian (Time-series).
