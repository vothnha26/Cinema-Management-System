-- =============================================================================
-- STAR CINEMA - ROOM & SEAT SETUP SCRIPT (MASTER DATA EDITION)
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE seat_prices;
TRUNCATE TABLE seats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE room_types;
TRUNCATE TABLE seat_types;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. SEED MASTER DATA
INSERT INTO room_types (id, name) VALUES 
('HALL_2D', 'Standard 2D Hall'), 
('IMAX', 'IMAX Premium Hall'), 
('HALL_3D', '3D Hall'), 
('HALL_4DX', '4DX Motion Hall');

INSERT INTO seat_types (id, name) VALUES 
('STANDARD', 'Standard Seat'), 
('VIP', 'VIP Premium Seat'), 
('COUPLE', 'Sweetbox Couple Seat');

-- 2. SEED ROOMS
-- Cinema 1: 5 rows x 10 cols = 50 seats
-- Cinema 2: 4 rows x 10 cols = 40 seats
INSERT INTO rooms (id, name, room_type_id, capacity, num_rows, num_cols, status) VALUES
(1, 'Cinema 1 Standard', 'HALL_2D', 50, 5, 10, 1),
(2, 'Cinema 2 Premium IMAX', 'IMAX', 40, 4, 10, 1);

-- 3. SEED SEATS FOR CINEMA 1 (ID: 1)
-- Rows A, B: STANDARD (20 seats)
INSERT INTO seats (room_id, row_char, col_num, seat_type_id, status) VALUES
(1, 'A', 1, 'STANDARD', 1), (1, 'A', 2, 'STANDARD', 1), (1, 'A', 3, 'STANDARD', 1), (1, 'A', 4, 'STANDARD', 1), (1, 'A', 5, 'STANDARD', 1),
(1, 'A', 6, 'STANDARD', 1), (1, 'A', 7, 'STANDARD', 1), (1, 'A', 8, 'STANDARD', 1), (1, 'A', 9, 'STANDARD', 1), (1, 'A', 10, 'STANDARD', 1),
(1, 'B', 1, 'STANDARD', 1), (1, 'B', 2, 'STANDARD', 1), (1, 'B', 3, 'STANDARD', 1), (1, 'B', 4, 'STANDARD', 1), (1, 'B', 5, 'STANDARD', 1),
(1, 'B', 6, 'STANDARD', 1), (1, 'B', 7, 'STANDARD', 1), (1, 'B', 8, 'STANDARD', 1), (1, 'B', 9, 'STANDARD', 1), (1, 'B', 10, 'STANDARD', 1);

-- Rows C, D: VIP (20 seats)
INSERT INTO seats (room_id, row_char, col_num, seat_type_id, status) VALUES
(1, 'C', 1, 'VIP', 1), (1, 'C', 2, 'VIP', 1), (1, 'C', 3, 'VIP', 1), (1, 'C', 4, 'VIP', 1), (1, 'C', 5, 'VIP', 1),
(1, 'C', 6, 'VIP', 1), (1, 'C', 7, 'VIP', 1), (1, 'C', 8, 'VIP', 1), (1, 'C', 9, 'VIP', 1), (1, 'C', 10, 'VIP', 1),
(1, 'D', 1, 'VIP', 1), (1, 'D', 2, 'VIP', 1), (1, 'D', 3, 'VIP', 1), (1, 'D', 4, 'VIP', 1), (1, 'D', 5, 'VIP', 1),
(1, 'D', 6, 'VIP', 1), (1, 'D', 7, 'VIP', 1), (1, 'D', 8, 'VIP', 1), (1, 'D', 9, 'VIP', 1), (1, 'D', 10, 'VIP', 1);

-- Row E: COUPLE (10 seats)
INSERT INTO seats (room_id, row_char, col_num, seat_type_id, status) VALUES
(1, 'E', 1, 'COUPLE', 1), (1, 'E', 2, 'COUPLE', 1), (1, 'E', 3, 'COUPLE', 1), (1, 'E', 4, 'COUPLE', 1), (1, 'E', 5, 'COUPLE', 1),
(1, 'E', 6, 'COUPLE', 1), (1, 'E', 7, 'COUPLE', 1), (1, 'E', 8, 'COUPLE', 1), (1, 'E', 9, 'COUPLE', 1), (1, 'E', 10, 'COUPLE', 1);

-- 4. SEED SEATS FOR CINEMA 2 IMAX (ID: 2)
-- Row A: STANDARD (10 seats)
INSERT INTO seats (room_id, row_char, col_num, seat_type_id, status) VALUES
(2, 'A', 1, 'STANDARD', 1), (2, 'A', 2, 'STANDARD', 1), (2, 'A', 3, 'STANDARD', 1), (2, 'A', 4, 'STANDARD', 1), (2, 'A', 5, 'STANDARD', 1),
(2, 'A', 6, 'STANDARD', 1), (2, 'A', 7, 'STANDARD', 1), (2, 'A', 8, 'STANDARD', 1), (2, 'A', 9, 'STANDARD', 1), (2, 'A', 10, 'STANDARD', 1);

-- Rows B, C, D: VIP (30 seats)
INSERT INTO seats (room_id, row_char, col_num, seat_type_id, status) VALUES
(2, 'B', 1, 'VIP', 1), (2, 'B', 2, 'VIP', 1), (2, 'B', 3, 'VIP', 1), (2, 'B', 4, 'VIP', 1), (2, 'B', 5, 'VIP', 1),
(2, 'B', 6, 'VIP', 1), (2, 'B', 7, 'VIP', 1), (2, 'B', 8, 'VIP', 1), (2, 'B', 9, 'VIP', 1), (2, 'B', 10, 'VIP', 1),
(2, 'C', 1, 'VIP', 1), (2, 'C', 2, 'VIP', 1), (2, 'C', 3, 'VIP', 1), (2, 'C', 4, 'VIP', 1), (2, 'C', 5, 'VIP', 1),
(2, 'C', 6, 'VIP', 1), (2, 'C', 7, 'VIP', 1), (2, 'C', 8, 'VIP', 1), (2, 'C', 9, 'VIP', 1), (2, 'C', 10, 'VIP', 1),
(2, 'D', 1, 'VIP', 1), (2, 'D', 2, 'VIP', 1), (2, 'D', 3, 'VIP', 1), (2, 'D', 4, 'VIP', 1), (2, 'D', 5, 'VIP', 1),
(2, 'D', 6, 'VIP', 1), (2, 'D', 7, 'VIP', 1), (2, 'D', 8, 'VIP', 1), (2, 'D', 9, 'VIP', 1), (2, 'D', 10, 'VIP', 1);

-- 5. SEED SEAT PRICES
INSERT INTO seat_prices (room_type_id, seat_type_id, price, effective_date, is_active) VALUES
('HALL_2D', 'STANDARD', 65000.00, '2024-01-01', 1),
('HALL_2D', 'VIP', 85000.00, '2024-01-01', 1),
('HALL_2D', 'COUPLE', 160000.00, '2024-01-01', 1),
('IMAX', 'STANDARD', 120000.00, '2024-01-01', 1),
('IMAX', 'VIP', 180000.00, '2024-01-01', 1);

-- AUDIT LOG
INSERT INTO audit_logs (username, action, target, target_id, details, timestamp) VALUES
('SYSTEM', 'SETUP', 'ROOM_SEAT_V2', 0, 'Room/Seat structure with Master Data tables initialized', NOW());
