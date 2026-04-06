-- =============================================================================
-- STAR CINEMA - DATABASE CLEAN & SEED SCRIPT (STRICT MODE)
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. TRUNCATE ALL TABLES TO START FRESH
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE booking_combos;
TRUNCATE TABLE booking_details;
TRUNCATE TABLE payments;
TRUNCATE TABLE bookings;
TRUNCATE TABLE notifications;
TRUNCATE TABLE pricing_rule_days;
TRUNCATE TABLE pricing_rules;
TRUNCATE TABLE seat_prices;
TRUNCATE TABLE seats;
TRUNCATE TABLE showtimes;
TRUNCATE TABLE movie_actors;
TRUNCATE TABLE movie_directors;
TRUNCATE TABLE movie_formats;
TRUNCATE TABLE movie_genres;
TRUNCATE TABLE movies;
TRUNCATE TABLE actors;
TRUNCATE TABLE directors;
TRUNCATE TABLE genres;
TRUNCATE TABLE formats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE promotions;
TRUNCATE TABLE membership_benefits;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
TRUNCATE TABLE combos;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. SEED CONFIGURATION & MASTER DATA
-- Membership Benefits
INSERT INTO membership_benefits (id, tier, discount_percent, point_multiplier) VALUES
(1, 'STANDARD', 0.0, 1.0),
(2, 'SILVER', 5.0, 1.2),
(3, 'GOLD', 10.0, 1.5),
(4, 'PLATINUM', 15.0, 2.0);

-- Formats
INSERT INTO formats (id, name) VALUES (1, '2D'), (2, '3D'), (3, 'IMAX'), (4, '4DX');

-- Genres
INSERT INTO genres (id, name) VALUES 
(1, 'Action'), (2, 'Comedy'), (3, 'Drama'), (4, 'Horror'), (5, 'Sci-Fi'), (6, 'Animation');

-- 3. SEED USER & CUSTOMER DATA
-- Pass: 123456 ($2a$10$eE1Z98wT18T0N8x6h/.iReK9I36d2u6H3i.D70q1U.m5dD0x.02fC)
INSERT INTO users (id, username, password, email, role, status, created_at) VALUES
(1, 'admin', '$2a$10$eE1Z98wT18T0N8x6h/.iReK9I36d2u6H3i.D70q1U.m5dD0x.02fC', 'admin@starcinema.com', 'ADMIN', 1, NOW()),
(2, 'staff1', '$2a$10$eE1Z98wT18T0N8x6h/.iReK9I36d2u6H3i.D70q1U.m5dD0x.02fC', 'staff1@starcinema.com', 'STAFF', 1, NOW()),
(3, 'customer1', '$2a$10$eE1Z98wT18T0N8x6h/.iReK9I36d2u6H3i.D70q1U.m5dD0x.02fC', 'customer1@gmail.com', 'CUSTOMER', 1, NOW());

INSERT INTO customers (id, user_id, full_name, phone, email, membership_tier, total_spending, points, created_at) VALUES
(1, 3, 'Nguyen Van A', '0901234567', 'customer1@gmail.com', 'STANDARD', 0, 0, NOW());

-- 4. SEED CINEMA STRUCTURE
-- Rooms
INSERT INTO rooms (id, name, type, capacity, num_rows, num_cols, status) VALUES
(1, 'Cinema 1', 'HALL_2D', 50, 5, 10, 1),
(2, 'Cinema 2 - IMAX', 'IMAX', 30, 3, 10, 1);

-- Seats for Cinema 1 (A1-A10, B1-B10 Standard; C1-C10 VIP)
INSERT INTO seats (room_id, row_char, col_num, type, status) 
SELECT 1, 'A', n, 'STANDARD', 1 FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t;
INSERT INTO seats (room_id, row_char, col_num, type, status) 
SELECT 1, 'B', n, 'STANDARD', 1 FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t;
INSERT INTO seats (room_id, row_char, col_num, type, status) 
SELECT 1, 'C', n, 'VIP', 1 FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t;

-- 5. SEED MOVIES & PRICING
-- Movies
INSERT INTO movies (id, title, description, duration, status, rating, age_rating, poster_url, trailer_url, priority_level, tmdb_id) VALUES
(1, 'Avengers: Secret Wars', 'The ultimate conclusion to the Multiverse Saga.', 180, 'SHOWING', 9.2, 'C13', 'https://image.tmdb.org/t/p/original/m9Y9S88mB3p9S1S9S1S9S1S9S1S.jpg', 'https://youtube.com/watch?v=123', 5, 299536),
(2, 'The Dark Knight Returns', 'Batman returns to Gotham to face a new threat.', 152, 'SHOWING', 8.5, 'C16', 'https://image.tmdb.org/t/p/original/qJ2tW6WMUDp9sDeJuuaS6kqH0v8.jpg', 'https://youtube.com/watch?v=456', 4, 155);

-- Mapping
INSERT INTO movie_genres (movie_id, genre_id) VALUES (1, 1), (1, 5), (2, 1), (2, 3);
INSERT INTO movie_formats (movie_id, format_id) VALUES (1, 1), (1, 3), (2, 1);

-- Seat Prices
INSERT INTO seat_prices (room_type, seat_type, price, effective_date, is_active) VALUES
('HALL_2D', 'STANDARD', 65000.00, '2024-01-01', 1),
('HALL_2D', 'VIP', 85000.00, '2024-01-01', 1),
('IMAX', 'STANDARD', 120000.00, '2024-01-01', 1),
('IMAX', 'VIP', 160000.00, '2024-01-01', 1);

-- Showtimes
INSERT INTO showtimes (id, movie_id, room_id, format_id, start_time, end_time, status, total_seats) VALUES
(1, 1, 1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY + INTERVAL 180 MINUTE), 'UPCOMING', 50),
(2, 2, 1, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY + INTERVAL 152 MINUTE), 'UPCOMING', 50);

-- 6. SEED COMMERCE DATA
-- Combos
INSERT INTO combos (id, name, description, price, stock_quantity, is_active) VALUES
(1, 'Solo Combo', '1 Popcorn Medium + 1 Coke', 75000.00, 100, 1),
(2, 'Couple Combo', '1 Popcorn Large + 2 Coke', 115000.00, 100, 1);

-- Promotions (Sửa cột min_benefit_id)
INSERT INTO promotions (id, code, name, discount_type, discount_value, min_benefit_id, start_date, end_date, is_active, min_order_amount) VALUES
(1, 'WELCOME10', 'Welcome Discount 10%', 'PERCENT', 10.00, 1, '2024-01-01', '2026-12-31', 1, 0.00),
(2, 'SILVER20K', 'Silver Tier Special 20k', 'FIXED', 20000.00, 2, '2024-01-01', '2026-12-31', 1, 100000.00);

-- Audit Log
INSERT INTO audit_logs (username, action, target, targetId, details, timestamp) VALUES
('SYSTEM', 'DATABASE_CLEAN', 'SYSTEM', 0, 'Database cleaned and re-seeded with standard data', NOW());
