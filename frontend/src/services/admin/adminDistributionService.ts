import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface BranchMovieData {
  id: number;
  branch?: {
    id: number;
    name: string;
    city: string;
  };
  movie: {
    id: number;
    title: string;
    posterUrl?: string;
    duration: number;
    ageRating: string;
  };
  priority: number;
  assignedAt: string;
  isActive: boolean;
}

export const adminDistributionService = {
  getBranchMovies: async (branchId: number) => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.BRANCH_MOVIES}/branch/${branchId}`);
    return res.data;
  },

  addMovieToBranch: async (branchId: number, movieId: number, priority: number = 5) => {
    const res = await api.post(ADMIN_API_ENDPOINTS.BRANCH_MOVIES, {
      branchId,
      movieId,
      priority,
      status: 'NOW_SHOWING'
    });
    return res.data;
  },

  removeMovieFromBranch: async (bmId: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.BRANCH_MOVIES}/${bmId}`);
    return res.data;
  },

  updateMoviePriority: async (bmId: number, priority: number) => {
    const res = await api.patch(`${ADMIN_API_ENDPOINTS.BRANCH_MOVIES}/${bmId}/priority`, null, {
      params: { priority }
    });
    return res.data;
  },
};
