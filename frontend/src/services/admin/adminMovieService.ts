import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface MovieRequestData {
  id?: number;
  title: string;
  description: string;
  duration: number; // phút
  genre: string;
  releaseDate: string; // ISO Date String
  language: string;
  director: string;
  cast: string;
  trailerUrl?: string;
  ageRating: string;
  status: 'NOW_SHOWING' | 'COMING_SOON' | 'END_OF_SHOW';
}

export const adminMovieService = {
  getMovies: async () => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.MOVIES}`);
    return res.data;
  },

  createMovie: async (data: MovieRequestData, posterFile?: File) => {
    const formData = new FormData();
    formData.append(
      'movie',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );
    if (posterFile) {
      formData.append('poster', posterFile);
    }
    const res = await api.post(`${ADMIN_API_ENDPOINTS.MOVIES}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  updateMovie: async (id: number, data: MovieRequestData, posterFile?: File) => {
    const formData = new FormData();
    formData.append(
      'movie',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );
    if (posterFile) {
      formData.append('poster', posterFile);
    }
    const res = await api.put(`${ADMIN_API_ENDPOINTS.MOVIES}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  deleteMovie: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.MOVIES}/${id}`);
    return res.data;
  },

  updatePriority: async (id: number, level: number) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.MOVIES}/${id}/priority`, null, {
      params: { level },
    });
    return res.data;
  },
};
