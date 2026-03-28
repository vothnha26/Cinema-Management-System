-- ==========================================================
-- STAR_CINEMA DATABASE MIGRATION SCRIPT - V1 (FIXED)
-- Mục tiêu: Chuẩn hóa Enum bằng cách nới lỏng kiểu dữ liệu trước khi ép kiểu
-- ==========================================================

USE cinema_db;

-- BƯỚC 1: NỚI LỎNG KIỂU DỮ LIỆU (Tránh lỗi Data truncated)
ALTER TABLE rooms MODIFY COLUMN type VARCHAR(255) NOT NULL;
ALTER TABLE seat_prices MODIFY COLUMN room_type VARCHAR(255) NOT NULL;
ALTER TABLE promotions MODIFY COLUMN discount_type VARCHAR(255) NOT NULL;
ALTER TABLE movies MODIFY COLUMN status VARCHAR(255) NOT NULL;

-- BƯỚC 2: CẬP NHẬT DỮ LIỆU SANG CHUẨN MỚI
-- 2.1. Rooms
UPDATE rooms SET type = 'FOUR_DX' WHERE type = 'HALL_4DX';
UPDATE rooms SET type = 'HALL_2D' WHERE type = 'STANDARD_2D';
UPDATE rooms SET type = 'HALL_3D' WHERE type = 'PREMIUM_3D';

-- 2.2. Seat Prices
UPDATE seat_prices SET room_type = 'FOUR_DX' WHERE room_type = 'HALL_4DX';
UPDATE seat_prices SET room_type = 'HALL_2D' WHERE room_type = 'STANDARD_2D';
UPDATE seat_prices SET room_type = 'HALL_3D' WHERE room_type = 'PREMIUM_3D';

-- 2.3. Promotions
UPDATE promotions SET discount_type = 'PERCENT' WHERE discount_type = 'PERCENTAGE';
UPDATE promotions SET discount_type = 'FIXED' WHERE discount_type = 'FIXED_AMOUNT';

-- 2.4. Movies (Dọn dẹp rating cũ sang age_rating)
UPDATE movies SET rating = NULL WHERE rating NOT IN ('P', 'K', 'T13', 'T16', 'T18');


-- BƯỚC 3: ÉP KIỂU VỀ ENUM CHUẨN ELITE
-- 3.1. Rooms
ALTER TABLE rooms 
MODIFY COLUMN type ENUM('HALL_2D', 'HALL_3D', 'IMAX', 'FOUR_DX', 'LUXURY') NOT NULL;

-- 3.2. Seat Prices
ALTER TABLE seat_prices 
MODIFY COLUMN room_type ENUM('HALL_2D', 'HALL_3D', 'IMAX', 'FOUR_DX', 'LUXURY') NOT NULL;

-- 3.3. Promotions
ALTER TABLE promotions 
MODIFY COLUMN discount_type ENUM('PERCENT', 'FIXED') NOT NULL;

-- 3.4. Movies
ALTER TABLE movies 
MODIFY COLUMN status ENUM('COMING_SOON', 'PRE_RELEASE', 'NOW_SHOWING', 'STOPPED') NOT NULL;

ALTER TABLE movies 
CHANGE COLUMN rating age_rating ENUM('P', 'K', 'T13', 'T16', 'T18');

-- ==========================================================
-- HOÀN TẤT CẬP NHẬT
-- ==========================================================
