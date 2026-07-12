import api from '../../config/api';

export interface BookingData {
  bookingCode: string;
  customerName?: string;
  customerPhone?: string;
  movieTitle: string;
  showTime: string;
  roomName: string;
  seatCodes: string[];
  comboSummary?: string[];
  totalPrice: number;
  status: 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'CANCELLED';
}

export const adminBookingService = {
  getAllBookings: async () => {
    const res = await api.get('/bookings/admin/all');
    return res.data;
  },

  checkInBooking: async (bookingCode: string) => {
    const res = await api.put(`/bookings/${bookingCode}/checkin`, {});
    return res.data;
  },
};
