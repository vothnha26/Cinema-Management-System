-- Cinema Management System - Ultimate Seed Data Script
USE cinema_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking_combos;
TRUNCATE TABLE booking_details;
TRUNCATE TABLE payments;
TRUNCATE TABLE bookings;
TRUNCATE TABLE notifications;
TRUNCATE TABLE promotions;
TRUNCATE TABLE combos;
TRUNCATE TABLE showtimes;
TRUNCATE TABLE seat_prices;
TRUNCATE TABLE seats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE movie_actors;
TRUNCATE TABLE movie_directors;
TRUNCATE TABLE movie_genres;
TRUNCATE TABLE movies;
TRUNCATE TABLE genres;
TRUNCATE TABLE actors;
TRUNCATE TABLE directors;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Table: users (15 records)
-- Passwords: password123
INSERT INTO users (id, username, password, email, role, status) VALUES 
(1, 'admin', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'vtn26xn@gmail.com', 'ADMIN', 1),
(2, 'manager', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'vothanhnha26@gmail.com', 'MANAGER', 1),
(3, 'staff_01', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'vothanhnha152@gmail.com', 'STAFF', 1),
(4, 'staff_02', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'staff2@cinema.com', 'STAFF', 1),
(5, 'customer_01', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'tung@gmail.com', 'CUSTOMER', 1),
(6, 'customer_02', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'lan@gmail.com', 'CUSTOMER', 1),
(7, 'customer_03', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'hoa@gmail.com', 'CUSTOMER', 1),
(8, 'customer_04', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'minh@gmail.com', 'CUSTOMER', 1),
(9, 'customer_05', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'anh@gmail.com', 'CUSTOMER', 1),
(10, 'customer_06', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'vy@gmail.com', 'CUSTOMER', 1),
(11, 'customer_07', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'bac@gmail.com', 'CUSTOMER', 1),
(12, 'customer_08', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'nam@gmail.com', 'CUSTOMER', 1),
(13, 'customer_09', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'linh@gmail.com', 'CUSTOMER', 1),
(14, 'customer_10', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'quang@gmail.com', 'CUSTOMER', 1),
(15, 'customer_11', '$2a$12$R9h/lIPzHZ5fJTMkjcyLxO9ZshprW5rkMEdDQiZt.F8H4K4/6W962', 'thao@gmail.com', 'CUSTOMER', 1);

-- 2. Table: customers
INSERT INTO customers (id, user_id, full_name, phone, membership_tier, total_spending, points) VALUES 
(1, 5, 'Phan Thanh Tùng', '0901112221', 'PLATINUM', 6500000.00, 650),
(2, 6, 'Nguyễn Thị Lan', '0901112222', 'GOLD', 2500000.00, 250),
(3, 7, 'Trần Văn Hoa', '0901112223', 'SILVER', 800000.00, 80),
(4, 8, 'Lê Quang Minh', '0901112224', 'STANDARD', 100000.00, 10),
(5, 9, 'Đặng Ngọc Anh', '0901112225', 'STANDARD', 0.00, 0),
(6, 10, 'Hoàng Thảo Vy', '0901112226', 'GOLD', 3200000.00, 320),
(7, 11, 'Vũ Xuân Bắc', '0901112227', 'SILVER', 1200000.00, 120),
(8, 12, 'Bùi Tiến Nam', '0901112228', 'PLATINUM', 5500000.00, 550),
(9, 13, 'Phạm Diệu Linh', '0901112229', 'STANDARD', 450000.00, 45),
(10, 14, 'Trương Minh Quang', '0901112230', 'STANDARD', 0.00, 0),
(11, 15, 'Lý Phương Thảo', '0901112231', 'SILVER', 950000.00, 95);

-- 3. Table: directors
INSERT INTO directors (id, name) VALUES 
(1, 'Christopher Nolan'), (2, 'Denis Villeneuve'), (3, 'Greta Gerwig'), (4, 'Martin Scorsese'), 
(5, 'Quentin Tarantino'), (6, 'James Cameron'), (7, 'Steven Spielberg'), (8, 'Bong Joon-ho'), 
(9, 'Wes Anderson'), (10, 'Makoto Shinkai'), (11, 'Trần Anh Hùng'), (12, 'Victor Vũ'),
(13, 'Lý Hải'), (14, 'Trấn Thành'), (15, 'Nguyễn Quang Dũng');

-- 4. Table: actors
INSERT INTO actors (id, name) VALUES 
(1, 'Cillian Murphy'), (2, 'Timothée Chalamet'), (3, 'Zendaya'), (4, 'Ryan Gosling'), (5, 'Margot Robbie'), 
(6, 'Leonardo DiCaprio'), (7, 'Robert Downey Jr.'), (8, 'Tom Cruise'), (9, 'Scarlett Johansson'), 
(10, 'Florence Pugh'), (11, 'Song Kang-ho'), (12, 'Thái Hòa'), (13, 'Kaity Nguyễn'), (14, 'Kiều Minh Tuấn'), (15, 'Ninh Dương Lan Ngọc');

-- 5. Table: genres
INSERT INTO genres (id, name) VALUES 
(1, 'Hành động'), (2, 'Viễn tưởng'), (3, 'Chính kịch'), (4, 'Hài hước'), (5, 'Kinh dị'), 
(6, 'Hoạt hình'), (7, 'Lãng mạn'), (8, 'Gia đình'), (9, 'Tội phạm'), (10, 'Tâm lý');

-- 6. Table: movies (Standardized with JPA/Hibernate)
INSERT INTO movies (id, title, description, duration, release_date, status, age_rating, poster_url, trailer_url, priority_level) VALUES 
(1, 'Oppenheimer', 'Cha đẻ bom nguyên tử.', 180, '2023-07-21', 'STOPPED', 'T18', 'https://images.com/opp.jpg', 'https://youtube.com/opp', 3),
(2, 'Dune: Part Two', 'Paul Atreides phục thù.', 166, '2024-03-01', 'NOW_SHOWING', 'T13', 'https://images.com/dune2.jpg', 'https://youtube.com/dune2', 5),
(3, 'Barbie', 'Barbie đến thế giới thực.', 114, '2023-07-21', 'STOPPED', 'P', 'https://images.com/barbie.jpg', 'https://youtube.com/barbie', 1),
(4, 'Killers of the Flower Moon', 'Vụ án bộ tộc Osage.', 206, '2023-10-20', 'STOPPED', 'T18', 'https://images.com/killers.jpg', 'https://youtube.com/killers', 2),
(5, 'Suzume', 'Khóa chặt cửa thiên tai.', 122, '2023-03-10', 'STOPPED', 'P', 'https://images.com/suzume.jpg', 'https://youtube.com/suzume', 1),
(6, 'Mai', 'Tình yêu và định kiến.', 131, '2024-02-10', 'NOW_SHOWING', 'T18', 'https://images.com/mai.jpg', 'https://youtube.com/mai', 4),
(7, 'Lật Mặt 7', 'Một điều ước của mẹ.', 138, '2024-04-26', 'NOW_SHOWING', 'K', 'https://images.com/latmat7.jpg', 'https://youtube.com/latmat7', 5),
(8, 'Godzilla x Kong', 'Đế chế mới của Titan.', 115, '2024-03-29', 'NOW_SHOWING', 'T13', 'https://images.com/gxk.jpg', 'https://youtube.com/gxk', 2),
(9, 'Kung Fu Panda 4', 'Po và Thủ lĩnh tinh thần.', 94, '2024-03-08', 'NOW_SHOWING', 'P', 'https://images.com/kfp4.jpg', 'https://youtube.com/kfp4', 3),
(10, 'Muôn Vị Nhân Gian', 'Ẩm thực và tình yêu.', 135, '2024-03-22', 'NOW_SHOWING', 'T13', 'https://images.com/muonvi.jpg', 'https://youtube.com/muonvi', 1),
(11, 'Deadpool & Wolverine', 'Bùng nổ Marvel.', 127, '2024-07-26', 'COMING_SOON', 'T18', 'https://images.com/deadpool.jpg', 'https://youtube.com/deadpool', 5),
(12, 'Joker 2', 'Điên có đôi.', 140, '2024-10-04', 'COMING_SOON', 'T18', 'https://images.com/joker2.jpg', 'https://youtube.com/joker2', 4);

-- 7. Table: movie_genres
INSERT INTO movie_genres (movie_id, genre_id) VALUES 
(1,3), (1,9), (2,1), (2,2), (3,4), (3,7), (4,9), (4,3), (5,6), (5,2), (6,3), (6,7), (7,3), (7,8), (8,1), (8,2), (9,6), (9,4), (10,3), (10,7), (11,1), (12,10);

-- 8. Table: movie_directors
INSERT INTO movie_directors (movie_id, director_id, role) VALUES 
(1,1,'MAIN'), (2,2,'MAIN'), (3,3,'MAIN'), (4,4,'MAIN'), (5,10,'MAIN'), (6,14,'MAIN'), (7,13,'MAIN'), (8,7,'MAIN'), (9,6,'MAIN'), (10,11,'MAIN'), (11,1,'MAIN'), (12,4,'MAIN');

-- 9. Table: movie_actors
INSERT INTO movie_actors (movie_id, actor_id, character_name, display_order) VALUES 
(1,1,'Oppenheimer',1), (1,7,'Strauss',2), (2,2,'Paul',1), (2,3,'Chani',2), (3,5,'Barbie',1), (3,4,'Ken',2), (6,14,'Dương',1), (6,15,'Mai',2), (7,12,'Hai',1), (9,11,'Po',1);

-- 10. Table: rooms (Standardized with RoomType Enum)
INSERT INTO rooms (id, name, type, capacity, status) VALUES 
(1, 'Room 01', 'HALL_2D', 100, 1), (2, 'Room 02', 'HALL_2D', 100, 1), (3, 'Room 03', 'HALL_3D', 80, 1), 
(4, 'IMAX Special', 'IMAX', 150, 1), (5, 'Premium 4DX', 'FOUR_DX', 60, 1), (6, 'Room 06', 'HALL_2D', 120, 1), 
(7, 'Room 07', 'HALL_2D', 120, 1), (8, 'Room 08', 'HALL_3D', 80, 1), (9, 'IMAX HFR', 'IMAX', 200, 1), (10, 'Luxury', 'LUXURY', 40, 1);

-- 11. Table: seat_prices
INSERT INTO seat_prices (id, room_type, seat_type, price, effective_date, is_active) VALUES 
(1, 'HALL_2D', 'STANDARD', 80000.00, '2024-01-01', 1), (2, 'HALL_2D', 'VIP', 110000.00, '2024-01-01', 1), (3, 'HALL_2D', 'COUPLE', 200000.00, '2024-01-01', 1),
(4, 'HALL_3D', 'STANDARD', 120000.00, '2024-01-01', 1), (5, 'HALL_3D', 'VIP', 150000.00, '2024-01-01', 1), (6, 'IMAX', 'STANDARD', 180000.00, '2024-01-01', 1),
(7, 'IMAX', 'VIP', 250000.00, '2024-01-01', 1), (8, 'FOUR_DX', 'STANDARD', 220000.00, '2024-01-01', 1), (9, 'FOUR_DX', 'VIP', 280000.00, '2024-01-01', 1);

-- 12. Table: seats
INSERT INTO seats (id, room_id, row_char, col_num, type, status) VALUES 
(1, 1, 'A', 1, 'STANDARD', 1), (2, 1, 'A', 2, 'STANDARD', 1), (3, 1, 'E', 5, 'VIP', 1), (4, 1, 'K', 1, 'COUPLE', 1), (5, 4, 'F', 10, 'VIP', 1), 
(6, 4, 'F', 11, 'VIP', 1), (7, 5, 'C', 3, 'STANDARD', 1), (8, 5, 'G', 8, 'VIP', 1), (9, 2, 'B', 1, 'STANDARD', 1), (10, 2, 'B', 2, 'STANDARD', 1), (11, 9, 'H', 15, 'VIP', 1), (12, 10, 'A', 1, 'VIP', 1);

-- 13. Table: showtimes (Standardized with Status and Occupancy - Physical sold_seats removed)
INSERT INTO showtimes (id, movie_id, room_id, start_time, end_time, status, total_seats) VALUES 
(1, 2, 4, '2026-03-29 18:00:00', '2026-03-29 20:46:00', 'UPCOMING', 150),
(2, 6, 1, '2026-03-29 19:00:00', '2026-03-29 21:11:00', 'UPCOMING', 100),
(3, 7, 2, '2026-03-29 20:00:00', '2026-03-29 22:18:00', 'UPCOMING', 100),
(4, 8, 5, '2026-03-30 14:00:00', '2026-03-30 15:55:00', 'UPCOMING', 60),
(5, 9, 3, '2026-03-30 15:00:00', '2026-03-30 16:34:00', 'UPCOMING', 80),
(6, 10, 6, '2026-03-30 17:00:00', '2026-03-30 19:15:00', 'UPCOMING', 120),
(7, 2, 9, '2026-03-30 20:00:00', '2026-03-30 22:46:00', 'UPCOMING', 200), 
(8, 6, 7, '2026-03-31 18:30:00', '2026-03-31 20:41:00', 'UPCOMING', 120),
(9, 7, 1, '2026-03-31 19:00:00', '2026-03-31 21:18:00', 'UPCOMING', 100),
(10, 8, 4, '2026-03-31 21:00:00', '2026-03-31 22:55:00', 'UPCOMING', 150), 
(11, 1, 1, '2026-03-28 10:00:00', '2026-03-28 13:00:00', 'ENDED', 100),
(12, 3, 2, '2026-03-28 14:00:00', '2026-03-28 15:54:00', 'ENDED', 100),
(13, 2, 4, '2026-03-28 19:00:00', '2026-03-28 21:46:00', 'SHOWING', 150), 
(14, 10, 10, '2026-04-01 19:00:00', '2026-04-01 21:15:00', 'UPCOMING', 40),
(15, 9, 8, '2026-04-01 10:00:00', '2026-04-01 11:34:00', 'UPCOMING', 80);

-- 14. Table: combos (Standardized with stock_quantity and image_url)
INSERT INTO combos (id, name, description, price, image_url, stock_quantity, is_active) VALUES 
(1, 'Single', '1 Bắp M + 1 Nước L', 75000.00, 'https://images.com/combo1.jpg', 100, 1), 
(2, 'Couple', '1 Bắp L + 2 Nước L', 125000.00, 'https://images.com/combo2.jpg', 100, 1), 
(3, 'Family', '2 Bắp L + 4 Nước L', 280000.00, 'https://images.com/combo3.jpg', 50, 1),
(4, 'Premium', '1 Bắp phô mai + 1 Juice', 95000.00, 'https://images.com/combo4.jpg', 80, 1), 
(5, 'Kids', 'Bắp mini + Milo', 65000.00, 'https://images.com/combo5.jpg', 150, 1), 
(6, 'Night', 'Bắp L + Cafe', 110000.00, 'https://images.com/combo6.jpg', 60, 1),
(7, 'Party', '3 Bắp L + 6 Nước', 450000.00, 'https://images.com/combo7.jpg', 30, 1), 
(8, 'Hotdog', 'Hotdog + Nước L', 85000.00, 'https://images.com/combo8.jpg', 100, 1), 
(9, 'Nachos', 'Nachos + Nước L', 90000.00, 'https://images.com/combo9.jpg', 100, 1), 
(10, 'Healthy', 'Salad + Suối', 70000.00, 'https://images.com/combo10.jpg', 40, 1);

-- 15. Table: promotions
INSERT INTO promotions (id, code, name, discount_type, discount_value, min_tier, start_date, end_date, is_active) VALUES 
(1, 'WELCOME20', 'Chào mừng', 'FIXED', 20000.00, 'STANDARD', '2024-01-01', '2026-12-31', 1), (2, 'PLATINUM15', 'VIP Platinum', 'PERCENT', 15.00, 'PLATINUM', '2024-01-01', '2026-12-31', 1),
(3, 'GOLD10', 'VIP Gold', 'PERCENT', 10.00, 'GOLD', '2024-01-01', '2026-12-31', 1), (4, 'SILVER5', 'VIP Silver', 'PERCENT', 5.00, 'SILVER', '2024-01-01', '2026-12-31', 1),
(5, 'STUDENT', 'Sinh viên', 'FIXED', 15000.00, 'STANDARD', '2024-01-01', '2026-12-31', 1), (6, 'MIDWEEK', 'Thứ 4', 'PERCENT', 20.00, 'STANDARD', '2024-01-01', '2026-12-31', 1),
(7, 'SUMMER24', 'Hè', 'PERCENT', 12.00, 'STANDARD', '2024-06-01', '2024-08-31', 1), (8, 'MOVIENIGHT', 'Đêm', 'FIXED', 30000.00, 'SILVER', '2024-01-01', '2026-12-31', 1),
(9, 'COMBOOFF', 'Giảm Combo', 'FIXED', 10000.00, 'STANDARD', '2024-01-01', '2026-12-31', 1), (10, 'BIRTHDAY', 'Sinh nhật', 'PERCENT', 50.00, 'GOLD', '2024-01-01', '2026-12-31', 1);

-- 16. Table: bookings
INSERT INTO bookings (id, customer_id, showtime_id, promotion_id, booking_code, total_price, status) VALUES 
(1, 1, 1, 2, 'BOK001', 450000.00, 'CONFIRMED'), (2, 2, 2, 3, 'BOK002', 320000.00, 'CONFIRMED'), (3, 3, 3, 4, 'BOK003', 150000.00, 'CHECKED_IN'),
(4, 4, 4, 1, 'BOK004', 280000.00, 'PENDING'), (5, 6, 5, 3, 'BOK005', 210000.00, 'CONFIRMED'), (6, 7, 6, 4, 'BOK006', 110000.00, 'CANCELLED'),
(7, 8, 7, 2, 'BOK007', 520000.00, 'CONFIRMED'), (8, 1, 8, NULL, 'BOK008', 350000.00, 'CONFIRMED'), (9, 2, 9, 3, 'BOK009', 180000.00, 'CHECKED_IN'),
(10, 11, 10, NULL, 'BOK010', 250000.00, 'CONFIRMED'), (11, 1, 11, NULL, 'BOK011', 160000.00, 'CHECKED_IN'), (12, 3, 12, 4, 'BOK012', 120000.00, 'CHECKED_IN'),
(13, 8, 13, 2, 'BOK013', 480000.00, 'CONFIRMED'), (14, 6, 14, 3, 'BOK014', 220000.00, 'PENDING'), (15, 7, 15, 4, 'BOK015', 135000.00, 'CONFIRMED');

-- 17. Table: booking_details (Expanded to populate dynamic soldSeats - Showing realistic occupancy)
INSERT INTO booking_details (id, booking_id, seat_id, price) VALUES 
(1,1,5,250000.00), (2,1,6,250000.00), (3,2,1,80000.00), (4,3,9,80000.00), (5,4,8,280000.00), 
(6,7,11,250000.00), (7,10,5,250000.00), (8,13,5,250000.00), (9,15,3,110000.00);

-- Generating high occupancy for Showtime ID=7 (Dune) via Seed Data
-- Showtime 7 has 200 total_seats. Let's add more details to trigger the "Orange" status (>90%)
INSERT INTO booking_details (booking_id, seat_id, price) SELECT 7, id, 180000.00 FROM seats WHERE id BETWEEN 1 AND 12;

-- 18. Table: booking_combos
INSERT INTO booking_combos (id, booking_id, combo_id, quantity, price) VALUES 
(1, 1, 2, 1, 125000.00), (2, 1, 4, 2, 95000.00), (3, 1, 8, 1, 85000.00),
(4, 2, 1, 1, 75000.00), (5, 2, 10, 1, 70000.00),
(6, 3, 5, 2, 65000.00), (7, 4, 3, 1, 280000.00), (8, 4, 9, 1, 90000.00),
(9, 5, 4, 1, 95000.00), (10, 5, 6, 1, 110000.00),
(11, 7, 3, 1, 280000.00), (12, 7, 7, 1, 450000.00),
(13, 8, 2, 2, 125000.00), (14, 9, 1, 3, 75000.00), (15, 9, 10, 2, 70000.00),
(16, 10, 6, 1, 110000.00), (17, 10, 8, 1, 85000.00),
(18, 11, 5, 1, 65000.00), (19, 12, 4, 1, 95000.00), (20, 12, 9, 1, 90000.00),
(21, 13, 7, 1, 450000.00), (22, 13, 2, 1, 125000.00),
(23, 14, 3, 1, 280000.00), (24, 15, 1, 1, 75000.00), (25, 15, 10, 1, 70000.00);

-- 19. Table: payments
INSERT INTO payments (id, booking_id, amount, payment_method, payment_status, transaction_id, paid_at) VALUES 
(1, 1, 450000.00, 'MOMO', 'SUCCESS', 'TXN_001', '2026-03-28 10:00:00'), 
(2, 2, 320000.00, 'VNPAY', 'SUCCESS', 'TXN_002', '2026-03-28 11:00:00'),
(3, 3, 150000.00, 'CASH', 'SUCCESS', NULL, '2026-03-28 19:30:00'), 
(4, 4, 280000.00, 'CARD', 'PENDING', NULL, NULL),
(5, 5, 210000.00, 'ZALOPAY', 'SUCCESS', 'TXN_005', '2026-03-28 12:00:00'), 
(6, 6, 110000.00, 'MOMO', 'FAILED', NULL, NULL),
(7, 7, 520000.00, 'CARD', 'SUCCESS', 'TXN_007', '2026-03-28 14:00:00'), 
(8, 8, 350000.00, 'VNPAY', 'SUCCESS', 'TXN_008', '2026-03-28 15:00:00'),
(9, 9, 180000.00, 'CASH', 'SUCCESS', NULL, '2026-03-28 18:00:00'), 
(10, 10, 250000.00, 'MOMO', 'SUCCESS', 'TXN_010', '2026-03-28 16:00:00'),
(11, 11, 160000.00, 'CASH', 'SUCCESS', NULL, '2026-03-28 09:30:00'), 
(12, 12, 120000.00, 'CASH', 'SUCCESS', NULL, '2026-03-28 13:30:00'),
(13, 13, 480000.00, 'CARD', 'SUCCESS', 'TXN_013', '2026-03-28 18:30:00'), 
(14, 14, 220000.00, 'ZALOPAY', 'PENDING', NULL, NULL),
(15, 15, 135000.00, 'MOMO', 'SUCCESS', 'TXN_015', '2026-03-28 20:00:00');

-- 20. Table: notifications
INSERT INTO notifications (id, user_id, title, message, type, is_read) VALUES 
(1, 5, 'Thành công', 'Vé BOK001 confirmed.', 'BOOKING', 1), (2, 6, 'Hạng Vàng', 'Chúc mừng lên hạng!', 'SYSTEM', 0),
(3, 1, 'Báo cáo', 'Doanh thu ngày 28/03.', 'SYSTEM', 0), (4, 5, 'Nhắc hẹn', 'Dune 2 chiếu sau 30p.', 'REMINDER', 0),
(5, 7, 'Khuyến mãi', 'Nhập SUMMER24 giảm 12%.', 'PROMOTION', 1), (6, 8, 'Thất bại', 'Thanh toán BOK006 lỗi.', 'BOOKING', 1),
(7, 9, 'Chào mừng', 'Welcome to Cinema!', 'SYSTEM', 0), (8, 10, 'Sinh nhật', 'Giảm 50% tháng này.', 'PROMOTION', 0),
(9, 11, 'Sử dụng', 'Check-in BOK009 xong.', 'BOOKING', 1), (10, 12, 'Hạng VIP', 'Chào mừng Platinum.', 'SYSTEM', 0),
(11, 13, 'Phim mới', 'Deadpool ra mắt tháng 7.', 'PROMOTION', 0), (12, 14, 'Bảo trì', 'Bảo trì lúc 2h sáng.', 'SYSTEM', 0),
(13, 15, 'Đã hủy', 'Vé BOK006 đã hủy.', 'BOOKING', 1), (14, 5, 'Điểm mới', '+50 điểm tích lũy.', 'BOOKING', 0), (15, 6, 'Gift code', 'Tặng mã COMBOOFF.', 'PROMOTION', 0);
