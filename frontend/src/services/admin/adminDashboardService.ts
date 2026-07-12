import api from '../../config/api';

export interface DashboardStatsRequest {
  startDate: string;
  endDate: string;
  startTime: string;
  endTime: string;
}

export const adminDashboardService = {
  getOverviewStats: async (params: DashboardStatsRequest) => {
    const res = await api.get('/statistics/overview', { params });
    return res.data;
  },
};
