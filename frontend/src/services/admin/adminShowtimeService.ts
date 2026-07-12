import api from '../../config/api';

export interface BulkShowtimeRequest {
  movieId: number;
  roomIds: number[];
  dates: string[];
  startTimes: string[];
  format: string;
}

export interface AISchedulingRequest {
  date: string;
  strategy: 'MAX_REVENUE' | 'MAX_OCCUPANCY' | 'BALANCED';
  branchId: number;
}

export const adminShowtimeService = {
  getShowtimes: async (date: string, branchId?: number) => {
    let url = `/showtimes?date=${date}`;
    if (branchId) {
      url += `&branchId=${branchId}`;
    }
    const res = await api.get(url);
    return res.data;
  },

  getShowtimeSeats: async (showtimeId: number) => {
    const res = await api.get(`/showtimes/${showtimeId}/seats`);
    return res.data;
  },

  bulkCreateShowtimes: async (data: BulkShowtimeRequest) => {
    const res = await api.post('/showtimes/bulk', data);
    return res.data;
  },

  deleteShowtime: async (id: number) => {
    const res = await api.delete(`/showtimes/${id}`);
    return res.data;
  },

  suggestAISchedule: async (data: AISchedulingRequest) => {
    const res = await api.post('/scheduling/suggest', data);
    return res.data;
  },

  applyAISchedule: async (draftId: string) => {
    const res = await api.post('/scheduling/apply', { draftId });
    return res.data;
  },
};
