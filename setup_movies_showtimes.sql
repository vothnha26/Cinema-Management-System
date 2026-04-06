-- =============================================================================
-- STAR CINEMA - MOVIE & SHOWTIME SETUP SCRIPT
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE showtimes;
TRUNCATE TABLE movie_genres;
TRUNCATE TABLE movie_formats;
TRUNCATE TABLE movie_actors;
TRUNCATE TABLE movie_directors;
TRUNCATE TABLE movies;
TRUNCATE TABLE genres;
TRUNCATE TABLE formats;
TRUNCATE TABLE actors;
TRUNCATE TABLE directors;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. SEED MASTER DATA
INSERT INTO formats (id, name) VALUES (1, '2D'), (2, '3D'), (3, 'IMAX'), (4, '4DX');

INSERT INTO genres (id, name) VALUES 
(1, 'Action'), (2, 'Adventure'), (3, 'Sci-Fi'), (4, 'Drama'), (5, 'Thriller'), (6, 'Biography'), (7, 'History');

INSERT INTO actors (id, name, avatar_url) VALUES 
(1, 'Robert Downey Jr.', 'https://image.tmdb.org/t/p/w500/5qHNST0qYSTuYSTu.jpg'),
(2, 'Cillian Murphy', 'https://image.tmdb.org/t/p/w500/hu8viSTuYSTuYSTu.jpg'),
(3, 'Christian Bale', 'https://image.tmdb.org/t/p/w500/bale.jpg'),
(4, 'Scarlett Johansson', 'https://image.tmdb.org/t/p/w500/scarlett.jpg');

INSERT INTO directors (id, name, avatar_url) VALUES 
(1, 'Christopher Nolan', 'https://image.tmdb.org/t/p/w500/nolan.jpg'),
(2, 'Anthony Russo', 'https://image.tmdb.org/t/p/w500/russo.jpg');

-- 2. SEED MOVIES
-- Avengers: Endgame (id: 1)
INSERT INTO movies (id, title, description, duration, release_date, status, rating, age_rating, poster_url, trailer_url, priority_level, tmdb_id) VALUES
(1, 'Avengers: Endgame', 'After the devastating events of Infinity War, the universe is in ruins.', 181, '2019-04-26', 'SHOWING', 8.4, 'T13', 'https://image.tmdb.org/t/p/original/or06vSydvS78o9mX98uAAnTS89.jpg', 'https://www.youtube.com/watch?v=TcMBFSGVi1c', 5, 299534);

-- Oppenheimer (id: 2)
INSERT INTO movies (id, title, description, duration, release_date, status, rating, age_rating, poster_url, trailer_url, priority_level, tmdb_id) VALUES
(2, 'Oppenheimer', 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.', 180, '2023-07-21', 'SHOWING', 8.9, 'T16', 'https://image.tmdb.org/t/p/original/8Gxv8S76be0dvTslm3pYI67S60W.jpg', 'https://www.youtube.com/watch?v=uYPbbksJxIg', 5, 872585);

-- 3. MAPPING MOVIE RELATIONS
-- Avengers Formats (2D, IMAX, 3D)
INSERT INTO movie_formats (movie_id, format_id) VALUES (1, 1), (1, 2), (1, 3);
-- Oppenheimer Formats (2D, IMAX)
INSERT INTO movie_formats (movie_id, format_id) VALUES (2, 1), (2, 3);

-- Genres
INSERT INTO movie_genres (movie_id, genre_id) VALUES (1, 1), (1, 2), (1, 3), (2, 4), (2, 6), (2, 7);

-- Actors (Character Names)
INSERT INTO movie_actors (movie_id, actor_id, character_name, display_order) VALUES 
(1, 1, 'Tony Stark / Iron Man', 1),
(1, 4, 'Natasha Romanoff / Black Widow', 2),
(2, 2, 'J. Robert Oppenheimer', 1),
(2, 1, 'Lewis Strauss', 2);

-- Directors
INSERT INTO movie_directors (movie_id, director_id, role) VALUES 
(1, 2, 'MAIN'),
(2, 1, 'MAIN');

-- 4. SEED SHOWTIMES (Linking to Rooms from Part 1)
-- Lịch chiếu Cinema 1 (ID 1) - Avengers (Format 2D)
INSERT INTO showtimes (id, movie_id, room_id, format_id, start_time, end_time, status, total_seats) VALUES
(1, 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL '09:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '12:01:00' HOUR_SECOND), 'UPCOMING', 50),
(2, 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL '13:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '16:01:00' HOUR_SECOND), 'UPCOMING', 50),
(3, 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL '19:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '22:01:00' HOUR_SECOND), 'UPCOMING', 50);

-- Lịch chiếu Cinema 2 IMAX (ID 2) - Oppenheimer (Format IMAX)
INSERT INTO showtimes (id, movie_id, room_id, format_id, start_time, end_time, status, total_seats) VALUES
(4, 2, 2, 3, DATE_ADD(CURDATE(), INTERVAL '08:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '11:00:00' HOUR_SECOND), 'UPCOMING', 40),
(5, 2, 2, 3, DATE_ADD(CURDATE(), INTERVAL '14:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '17:00:00' HOUR_SECOND), 'UPCOMING', 40),
(6, 2, 2, 3, DATE_ADD(CURDATE(), INTERVAL '20:00:00' HOUR_SECOND), DATE_ADD(CURDATE(), INTERVAL '23:00:00' HOUR_SECOND), 'UPCOMING', 40);

-- AUDIT LOG
INSERT INTO audit_logs (username, action, target, target_id, details, timestamp) VALUES
('SYSTEM', 'SETUP', 'MOVIE_SHOWTIME', 0, 'Detailed movie catalog and showtimes initialized', NOW());
