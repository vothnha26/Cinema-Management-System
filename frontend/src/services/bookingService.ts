import api from '../config/api';

export const bookingService = {
  /**
   * Tạo một booking mới (giữ ghế tạm thời)
   */
  async createBooking(payload: {
    showtimeId: number;
    seatIds: number[];
    combos?: { comboId: number; quantity: number }[];
    promoCode?: string | null;
    customerId?: number | null;
    paymentMethod: string;
    sessionId?: string;
  }) {
    const response = await api.post('/bookings', payload);
    return response.data;
  },

  /**
   * Lấy chi tiết booking theo ID (thông tin vé, số tiền, QR code, thời gian giữ ghế)
   */
  async getBookingDetails(bookingId: number | string) {
    const response = await api.get(`/bookings/${bookingId}`);
    return response.data;
  },

  /**
   * Lấy lịch sử đặt vé của user hiện tại
   */
  async getBookingHistory() {
    const response = await api.get('/bookings/me');
    return response.data;
  },

  /**
   * Lấy danh sách ghế đang khóa của phiên hiện tại
   */
  async getMyLockedSeats(showtimeId: number | string, sessionId: string) {
    const response = await api.get(`/bookings/my-locked-seats?showtimeId=${showtimeId}&sessionId=${sessionId}`);
    return response.data;
  },

  /**
   * Lấy danh sách combo bắp nước
   */
  async getCombos() {
    const response = await api.get('/combos');
    return response.data;
  },

  /**
   * Khóa ghế tạm thời
   */
  async holdSeat(showtimeId: number | string, seatId: number, sessionId: string) {
    const response = await api.post(`/bookings/hold-seat?showtimeId=${showtimeId}&seatId=${seatId}&sessionId=${sessionId}`);
    return response.data;
  },

  /**
   * Mở khóa ghế tạm thời
   */
  async releaseSeat(showtimeId: number | string, seatId: number, sessionId: string) {
    const response = await api.post(`/bookings/release-seat?showtimeId=${showtimeId}&seatId=${seatId}&sessionId=${sessionId}`);
    return response.data;
  },

  /**
   * Kiểm tra mã khuyến mãi
   */
  async checkPromotion(code: string) {
    const response = await api.get(`/promotions/check?code=${code}`);
    return response.data;
  },
};
