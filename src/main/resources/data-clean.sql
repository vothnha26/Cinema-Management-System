-- Seed users
INSERT INTO users (id, username, password, email, role, status, created_at) VALUES
(1, 'customer1', '$2a$10$eE1Z98wT18T0N8x6h/.iReK9I36d2u6H3i.D70q1U.m5dD0x.02fC', 'customer1@gmail.com', 'CUSTOMER', 1, NOW()),
(99, 'admin', '$2a$10$8.069yx77j/l/s.W8T44I.n/F.OqYIu/sY4aT/vK5uE6f6U5eIu2q', 'admin@starcinema.com', 'ADMIN', 1, NOW()),
(100, 'admin2', '$2a$10$mC7p3YXY/8PP6v9zBphFveN5Vf7.GInU9T05tF.7tF.7tF.7tF.7tF', 'admin2@starcinema.com', 'ADMIN', 1, NOW())
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    email = VALUES(email),
    role = VALUES(role),
    status = VALUES(status);

-- Seed customer
INSERT IGNORE INTO customers (id, user_id, full_name, phone, membership_tier, total_spending, points)
VALUES (1, 1, 'Nguyen Van A', '0901234567', 'STANDARD', 0, 0);

-- Seed movies
INSERT INTO movies (id, title, description, duration, status, rating, age_rating, poster_url, tmdb_id) VALUES
(1, 'Avengers: Secret Wars', 'Tran chien cuoi cung giua cac Avengers va ke phan dien.', 160, 'SHOWING', '9.0', 'C13', 'https://cdn.marvel.com/content/2x/theavengers_lob_mas_dsk_03.jpg', 299536),
(2, 'The Dark Knight Returns', 'Batman quay tro lai Gotham trong cuoc chien voi bong toi.', 152, 'SHOWING', '8.5', 'C16', 'https://image.tmdb.org/t/p/original/qJ2tW6WMUDp9sDeJuuaS6kqH0v8.jpg', 155)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    duration = VALUES(duration),
    status = VALUES(status),
    rating = VALUES(rating),
    age_rating = VALUES(age_rating),
    poster_url = VALUES(poster_url),
    tmdb_id = VALUES(tmdb_id);

-- Seed room
INSERT IGNORE INTO rooms (id, name, type, capacity, status)
VALUES (1, 'Cinema 1', 'HALL_2D', 50, 1);

-- Seed seats
INSERT IGNORE INTO seats (id, room_id, row_char, col_num, type, status) VALUES
(1, 1, 'A', 1, 'STANDARD', 1), (2, 1, 'A', 2, 'STANDARD', 1), (3, 1, 'A', 3, 'STANDARD', 1),
(4, 1, 'A', 4, 'STANDARD', 1), (5, 1, 'A', 5, 'STANDARD', 1),
(6, 1, 'B', 1, 'VIP', 1), (7, 1, 'B', 2, 'VIP', 1), (8, 1, 'B', 3, 'VIP', 1),
(9, 1, 'B', 4, 'VIP', 1), (10, 1, 'B', 5, 'VIP', 1);

-- Seed showtime
INSERT IGNORE INTO showtimes (id, movie_id, room_id, start_time, end_time, status) VALUES
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 27 HOUR), 'UPCOMING');

-- Seed combos
INSERT IGNORE INTO combos (id, name, description, price, is_active) VALUES
(1, 'Combo 1 Big', '1 bap lon + 1 nuoc ngot', 75000, 1),
(2, 'Combo 2 Extra', '1 bap lon + 2 nuoc ngot', 95000, 1);

-- Seed active promotions
INSERT INTO promotions (id, code, name, discount_type, discount_value, min_tier, start_date, end_date, is_active) VALUES
(1, 'WELCOME10', 'Giam 10 phan tram cho don dau tien', 'PERCENT', 10.00, 'STANDARD', '2026-01-01', '2026-12-31', 1),
(2, 'SILVER50K', 'Giam 50.000d cho thanh vien Silver tro len', 'FIXED', 50000.00, 'SILVER', '2026-01-01', '2026-12-31', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    discount_type = VALUES(discount_type),
    discount_value = VALUES(discount_value),
    min_tier = VALUES(min_tier),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    is_active = VALUES(is_active);

-- Additional seat prices
INSERT IGNORE INTO seat_prices (room_type, seat_type, price, effective_date, is_active) VALUES
('HALL_2D', 'STANDARD', 60000, '2024-01-01', 1),
('HALL_2D', 'VIP', 90000, '2024-01-01', 1),
('HALL_2D', 'COUPLE', 150000, '2024-01-01', 1),
('IMAX', 'STANDARD', 150000, '2024-01-01', 1),
('IMAX', 'VIP', 220000, '2024-01-01', 1),
('HALL_4DX', 'STANDARD', 180000, '2024-01-01', 1),
('HALL_4DX', 'VIP', 250000, '2024-01-01', 1);
