# Thiết kế Cơ sở dữ liệu - Nhóm 3: Phim & Nội dung chi tiết

Nhóm này quản lý kho phim, lịch chiếu và các thực thể liên quan đến nội dung điện ảnh.

---

## 14. Bảng: movies (Thông tin Phim)
Hồ sơ chính của các bộ phim.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh phim | PK, Auto Increment |
| 2 | title | VARCHAR(255) | - | Tên phim | Not Null |
| 3 | description | TEXT | - | Tóm tắt nội dung | - |
| 4 | duration | INT | > 0 | Thời lượng (phút) | Not Null |
| 5 | release_date | DATE | - | Ngày bắt đầu công chiếu | - |
| 6 | age_rating | VARCHAR(50) | P, K, T13, T16, T18 | Phân loại độ tuổi | - |
| 7 | status | VARCHAR(50) | COMING_SOON, SHOWING... | Trạng thái phát hành | - |
| 8 | tmdb_id | BIGINT | Duy nhất | ID đồng bộ từ TMDB API | Unique |
| 9 | origin_country | VARCHAR(100)| - | Quốc gia sản xuất (Vd: VN, US) | - |
| 10 | priority_level | INT | 1-10 | Độ ưu tiên xếp lịch AI | Mặc định: 1 |

---

## 15. Bảng: genres (Thể loại Phim)
Danh mục các thể loại (Hành động, Hài, Kinh dị...).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | VARCHAR(50) | - | Mã thể loại | PK |
| 2 | name | VARCHAR(255) | - | Tên thể loại | Not Null |

---

## 16. Bảng: formats (Định dạng Phim)
Các công nghệ chiếu phim (2D, 3D, 4DX, ScreenX...).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | VARCHAR(50) | - | Mã định dạng | PK |
| 2 | name | VARCHAR(255) | - | Tên định dạng hiển thị | Not Null |

---

## 17. Bảng: actors (Diễn viên)
Hồ sơ diễn viên điện ảnh.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã diễn viên | PK |
| 2 | name | VARCHAR(255) | - | Tên đầy đủ diễn viên | Not Null |
| 3 | avatar_url | VARCHAR(255) | - | Đường dẫn ảnh chân dung | - |

---

## 18. Bảng: directors (Đạo diễn)
Hồ sơ các đạo diễn.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã đạo diễn | PK |
| 2 | name | VARCHAR(255) | - | Tên đạo diễn | Not Null |
| 3 | avatar_url | VARCHAR(255) | - | Đường dẫn ảnh chân dung | - |

---

## 19. Bảng: showtimes (Suất chiếu)
Lịch chiếu phim cụ thể tại một phòng vào một khung giờ.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh suất chiếu | PK, Auto Increment |
| 2 | movie_id | BIGINT | - | Mã phim được chiếu | FK (movies.id) |
| 3 | room_id | BIGINT | - | Mã phòng chiếu | FK (rooms.id) |
| 4 | format_id | VARCHAR(50) | - | Định dạng chiếu thực tế | FK (formats.id) |
| 5 | start_time | DATETIME | - | Giờ bắt đầu chiếu | Not Null |
| 6 | end_time | DATETIME | - | Giờ kết thúc dự kiến | Not Null |
| 7 | status | VARCHAR(50) | ACTIVE, CANCELLED | Trạng thái vận hành | - |

---

## 20. Bảng: movie_actors (Liên kết Phim-Diễn viên)
Quản lý dàn diễn viên tham gia trong mỗi bộ phim.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id | BIGINT | - | Mã phim | PK, FK (movies.id) |
| 2 | actor_id | BIGINT | - | Mã diễn viên | PK, FK (actors.id) |

---

## 21. Bảng: movie_directors (Liên kết Phim-Đạo diễn)
Quản lý đạo diễn của phim.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id | BIGINT | - | Mã phim | PK, FK (movies.id) |
| 2 | director_id | BIGINT | - | Mã đạo diễn | PK, FK (directors.id) |

---

## 22. Bảng: movie_genres (Liên kết Phim-Thể loại)
Phân loại các thể loại cho một phim.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id | BIGINT | - | Mã phim | PK, FK (movies.id) |
| 2 | genre_id | VARCHAR(50) | - | Mã thể loại | PK, FK (genres.id) |

---

## 23. Bảng: movie_formats (Liên kết Phim-Định dạng)
Cấu hình các định dạng chiếu khả dụng cho phim.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id | BIGINT | - | Mã phim | PK, FK (movies.id) |
| 2 | format_id | VARCHAR(50) | - | Mã định dạng hỗ trợ | PK, FK (formats.id) |
