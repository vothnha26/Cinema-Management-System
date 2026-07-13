-- V2: Tối ưu hóa & bổ sung thuộc tính Database Schema

-- 1. Bổ sung các cột cho bảng bookings
ALTER TABLE bookings ADD COLUMN checked_in_at datetime(6) DEFAULT NULL;
ALTER TABLE bookings ADD COLUMN checked_in_by bigint DEFAULT NULL;
ALTER TABLE bookings ADD COLUMN discount_amount decimal(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE bookings ADD CONSTRAINT FK_bookings_checked_in_by FOREIGN KEY (checked_in_by) REFERENCES users (id) ON DELETE SET NULL;

-- 2. Bổ sung các cột cho bảng payments
ALTER TABLE payments ADD COLUMN refund_amount decimal(10,2) DEFAULT NULL;
ALTER TABLE payments ADD COLUMN refunded_at datetime(6) DEFAULT NULL;
ALTER TABLE payments ADD COLUMN refund_reason varchar(255) DEFAULT NULL;

-- 3. Bổ sung cột cho bảng membership_levels
ALTER TABLE membership_levels ADD COLUMN min_points int NOT NULL DEFAULT 0;

-- 4. Bổ sung các cột cho bảng branches
ALTER TABLE branches ADD COLUMN latitude double DEFAULT NULL;
ALTER TABLE branches ADD COLUMN longitude double DEFAULT NULL;
ALTER TABLE branches ADD COLUMN emergency_hotline varchar(255) DEFAULT NULL;
ALTER TABLE branches ADD COLUMN opening_hours varchar(255) DEFAULT NULL;

-- 5. Bổ sung cột cho bảng customers
ALTER TABLE customers ADD COLUMN date_of_birth date DEFAULT NULL;

-- 6. Bổ sung cột cho bảng movies
ALTER TABLE movies ADD COLUMN backdrop_url varchar(255) DEFAULT NULL;

-- 7. Bổ sung các cột cho bảng users
ALTER TABLE users ADD COLUMN email_verified tinyint(1) NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN last_login datetime(6) DEFAULT NULL;

-- 8. Bổ sung cột cho bảng promotions
ALTER TABLE promotions ADD COLUMN stock_quantity int DEFAULT NULL;

-- 9. Xóa cột giá và tồn kho khỏi bảng combos để tránh 2 nguồn sự thật (Master Data vs Operational Data)
ALTER TABLE combos DROP COLUMN price;
ALTER TABLE combos DROP COLUMN stock_quantity;

-- 10. Bổ sung các ràng buộc UNIQUE cho seats và rooms
ALTER TABLE seats ADD CONSTRAINT UK_room_seat_position UNIQUE (room_id, row_char, col_num);
ALTER TABLE rooms ADD CONSTRAINT UK_branch_room_name UNIQUE (branch_id, name);

-- 11. Bổ sung cột và khóa ngoại cho bảng audit_logs
ALTER TABLE audit_logs ADD COLUMN user_id bigint DEFAULT NULL;
ALTER TABLE audit_logs ADD CONSTRAINT FK_audit_logs_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;
