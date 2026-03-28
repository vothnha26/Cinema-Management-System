# 🗄️ Database – Cinema Management System (Elite Edition)

## 1. Tổng quan

- **DBMS**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Tổng số bảng**: 21 bảng (Bổ sung `audit_logs`)
- **Elite Integrity**: Sử dụng cơ chế tự động vô hiệu hóa giá cũ và kiểm tra xung đột logic trước khi lưu.

---

## 2. Các thay đổi Enum (Elite Standards)

Chúng ta đã chuẩn hóa các Enum để hỗ trợ nhiều tính năng nâng cao:

| Enum | Các giá trị mới/cập nhật | Mục đích |
|------|--------------------------|----------|
| **`MovieStatus`** | `COMING_SOON`, `PRE_RELEASE`, `NOW_SHOWING`, `STOPPED` | Hỗ trợ **Suất chiếu sớm** (Task 9). |
| **`AgeRating`** | `P`, `K`, `T13`, `T16`, `T18` | Theo chuẩn nhãn độ tuổi điện ảnh Việt Nam. |
| **`RoomType`** | `HALL_2D`, `HALL_3D`, `IMAX`, `FOUR_DX`, `LUXURY` | Đồng bộ tên gọi và hỗ trợ phòng hạng sang. |
| **`SeatType`** | `STANDARD`, `VIP`, `COUPLE`, `DISABLED` | Bổ sung `DISABLED` cho ghế hỏng hoặc lối đi. |
| **`DiscountType`** | `PERCENT`, `FIXED` | Rút gọn định dạng cho Promotion Engine. |

---

## 3. Bảng mới: `audit_logs` (Nhật ký hệ thống)

Bảng này được tự động điền dữ liệu thông qua Spring AOP Aspect.

```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,    -- Người thực hiện (từ SecurityContext)
    action VARCHAR(50) NOT NULL,      -- CREATE, UPDATE, DELETE, UPDATE_STOCK
    target VARCHAR(50) NOT NULL,      -- MOVIE, ROOM, SHOWTIME, PRICING, COMBO, PROMOTION
    target_id BIGINT,                 -- ID của đối tượng bị tác động
    details TEXT,                     -- Chi tiết hành động (ví dụ: "Thực hiện CREATE trên MOVIE")
    timestamp DATETIME NOT NULL       -- Thời điểm thực hiện
);
```

---

## 4. Cập nhật bảng `movies`

Trường `rating` (String) đã được thay thế hoàn toàn bằng `age_rating` (Enum).

```sql
-- Cấu trúc bảng movies mới
CREATE TABLE movies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    status ENUM('COMING_SOON', 'PRE_RELEASE', 'NOW_SHOWING', 'STOPPED') NOT NULL,
    age_rating ENUM('P', 'K', 'T13', 'T16', 'T18'),
    priority_level INT DEFAULT 1,     -- Phục vụ AI Scheduling (1-5)
    ...
);
```

---

## 5. Cập nhật bảng `combos`

Bổ sung quản lý tồn kho cho F&B.

```sql
ALTER TABLE combos ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0;
```

---

## 6. Elite Indexing & Optimization

```sql
-- Tìm nhật ký gần nhất
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp DESC);

-- Tìm giá vé đang hoạt động nhanh nhất (cho Pricing Engine)
CREATE INDEX idx_seat_price_active ON seat_prices(room_type, seat_type, is_active);

-- Tìm suất chiếu theo ngày để kiểm tra xung đột AI
CREATE INDEX idx_showtime_room_date ON showtimes(room_id, start_time);
```
