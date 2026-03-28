# 🗄️ Database – Cinema Management System

## 1. Tổng quan

- **DBMS**: MySQL 8.0
- **Charset**: `utf8mb4` (hỗ trợ tiếng Việt & emoji)
- **Engine**: InnoDB (hỗ trợ FK, transaction, ACID)
- **ORM**: Spring Data JPA / Hibernate
- **Tổng số bảng**: 20 bảng

---

## 2. ERD – Sơ đồ quan hệ (20 bảng)

```
┌──────────┐     ┌─────────────┐     ┌──────────┐
│ directors│────<│movie_director│>────│  movies  │
└──────────┘     └─────────────┘     └────┬─────┘
                                          │M:M
┌──────────┐     ┌─────────────┐          │
│  genres  │────<│ movie_genres │>─────────┤
└──────────┘     └─────────────┘          │M:M
                                          │
┌──────────┐     ┌─────────────┐          │
│  actors  │────<│ movie_actors │>─────────┘
└──────────┘     └─────────────┘

       movies ──────────────────────────┐
          │ 1                           │
          │ M                           │
      showtimes──── rooms ──── seats    │
          │ 1            1──M           │
          │ M                       [seat_prices]
       bookings ────────────────── customers ── users
          │ 1                   1      │         │ 1
          ├── M booking_details        │         │ 1
          ├── M booking_combos         │       [User 1:1 Customer]
          │        │                   │
          │       combos               │
          │ 1                   promotions
          │ 1
        payments

    users ──── M ──── notifications
```

---

## 3. Chi tiết từng bảng & Quan hệ

### Nhóm phim & danh mục

| Bảng | Quan hệ |
|------|---------|
| `movies` | Trung tâm – kết nối genres, actors, directors, showtimes |
| `genres` | M:M với movies qua `movie_genres` |
| `directors` | M:M với movies qua `movie_directors` |
| `actors` | M:M với movies qua `movie_actors` (có `character_name`) |
| `movie_genres` | Bảng join (composite PK: movie_id + genre_id) |
| `movie_directors` | Bảng join (có thêm cột `role`: MAIN/CO_DIRECTOR) |
| `movie_actors` | Bảng join (có `character_name`, `display_order`) |

### Nhóm rạp & ghế

| Bảng | Quan hệ |
|------|---------|
| `rooms` | 1 room → M seats |
| `seats` | M seats → 1 room; FK: `room_id` |
| `seat_prices` | Bảng giá vé: tra cứu theo `(room_type, seat_type)` |

```sql
-- seat_prices
CREATE TABLE seat_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type ENUM('2D','3D','IMAX','4DX') NOT NULL,
    seat_type ENUM('STANDARD','VIP','COUPLE') NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    effective_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY uq_roomtype_seattype (room_type, seat_type, effective_date)
);
```

### Nhóm đặt vé

```sql
-- bookings
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,              -- FK → customers
    showtime_id BIGINT NOT NULL,              -- FK → showtimes
    promotion_id BIGINT,                      -- FK → promotions (nullable)
    booking_code VARCHAR(20) UNIQUE NOT NULL, -- UUID ngắn
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','CANCELLED','CHECKED_IN') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- payments (1:1 với bookings)
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT UNIQUE NOT NULL,       -- FK → bookings
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH','CARD','MOMO','VNPAY','ZALOPAY'),
    payment_status ENUM('PENDING','SUCCESS','FAILED','REFUNDED'),
    transaction_id VARCHAR(100),             -- Mã giao dịch bên ngoài
    paid_at TIMESTAMP
);
```

### Nhóm khuyến mãi & thông báo

```sql
-- promotions
CREATE TABLE promotions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    discount_type ENUM('PERCENT','FIXED') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_tier ENUM('STANDARD','SILVER','GOLD','PLATINUM') DEFAULT 'STANDARD',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- notifications
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,                 -- FK → users
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('BOOKING','REMINDER','PROMOTION','SYSTEM'),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. Index quan trọng

```sql
-- Tìm suất chiếu theo phim & ngày
CREATE INDEX idx_showtime_movie_date ON showtimes(movie_id, start_time);

-- Kiểm tra ghế đã đặt (query hottest)
CREATE INDEX idx_booking_detail_showtime ON booking_details(booking_id);

-- Tra mã booking
CREATE UNIQUE INDEX idx_booking_code ON bookings(booking_code);

-- Thông báo của user
CREATE INDEX idx_notification_user ON notifications(user_id, is_read);

-- Tìm giá theo loại ghế/phòng
CREATE INDEX idx_seat_price ON seat_prices(room_type, seat_type, is_active);
```

---

## 5. ORM Mapping (JPA)

| Quan hệ DB | JPA Annotation |
|-----------|---------------|
| 1:M (Room → Seats) | `@OneToMany` / `@ManyToOne` |
| M:M (Movie ↔ Genre) | `@ManyToMany` + `@JoinTable` |
| 1:1 (User ↔ Customer) | `@OneToOne` + `@JoinColumn` |
| 1:1 (Booking ↔ Payment) | `@OneToOne(cascade = ALL)` |
| Cascade | BookingDetail, BookingCombo: `cascade = ALL, orphanRemoval = true` |
| Lazy loading | Mặc định LAZY cho tất cả quan hệ – tránh N+1 |

---

## 6. Enums trong Database

```java
// MovieStatus
COMING_SOON, NOW_SHOWING, STOPPED

// ShowtimeStatus
UPCOMING, SHOWING, ENDED, CANCELLED

// SeatType
STANDARD, VIP, COUPLE

// RoomType
HALL_2D, HALL_3D, IMAX, HALL_4DX

// BookingStatus
PENDING, CONFIRMED, CANCELLED, CHECKED_IN

// MembershipTier
STANDARD, SILVER, GOLD, PLATINUM

// PaymentMethod
CASH, CARD, MOMO, VNPAY, ZALOPAY

// Role
ADMIN, MANAGER, STAFF, CUSTOMER
```
