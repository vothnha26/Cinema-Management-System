import api from '../config/api';

export const movieService = {
  /**
   * Lấy danh sách tất cả phim dang chieu / sap chieu
   */
  async getMovies() {
    const response = await api.get('/public/movies');
    return response.data;
  },

  /**
   * Lấy danh sách phim đang chiếu
   */
  async getNowPlaying() {
    const response = await api.get('/movies/showing');
    return response.data;
  },

  /**
   * Lấy danh sách phim sắp chiếu
   */
  async getUpcoming() {
    const response = await api.get('/movies/coming');
    return response.data;
  },

  /**
   * Lấy danh sách chi nhánh rạp
   */
  async getBranches() {
    const response = await api.get('/public/branches');
    return response.data;
  },

  /**
   * Lấy danh sách thể loại phim
   */
  async getGenres() {
    const response = await api.get('/public/genres');
    return response.data;
  },

  /**
   * Lấy thông tin chi tiết một bộ phim
   */
  async getMovieDetails(movieId: number | string) {
    const response = await api.get(`/movies/${movieId}`);
    return response.data;
  },

  /**
   * Lấy danh sách ngày có lịch chiếu của phim
   */
  async getShowtimeDates(movieId: number | string) {
    const response = await api.get(`/showtimes/dates?movieId=${movieId}`);
    return response.data;
  },

  /**
   * Lấy danh sách suất chiếu của phim theo ngày
   */
  async getShowtimes(movieId: number | string, date: string) {
    const response = await api.get(`/showtimes?movieId=${movieId}&date=${date}`);
    return response.data;
  },

  /**
   * Lấy lịch chiếu/suất chiếu của bộ phim
   */
  async getMovieShowtimes(movieId: number | string) {
    const response = await api.get(`/public/movies/${movieId}/showtimes`);
    return response.data;
  },

  /**
   * Lấy sơ đồ ghế của một suất chiếu
   */
  async getShowtimeSeats(showtimeId: number | string) {
    const response = await api.get(`/showtimes/${showtimeId}/seats`);
    return response.data;
  },

  /**
   * Lấy chi tiết một suất chiếu (phục vụ đặt vé: danh sách ghế, thông tin rạp)
   */
  async getShowtimeDetails(showtimeId: number | string) {
    const response = await api.get(`/showtimes/${showtimeId}`);
    return response.data;
  },
};
