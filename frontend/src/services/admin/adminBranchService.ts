import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface BranchData {
  id?: number;
  name: string;
  address: string;
  city: string;
  phone: string;
  isActive: boolean;
}

export const adminBranchService = {
  getBranches: async () => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.BRANCHES}`);
    return res.data;
  },

  createBranch: async (data: BranchData) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.BRANCHES}`, data);
    return res.data;
  },

  updateBranch: async (id: number, data: BranchData) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.BRANCHES}/${id}`, data);
    return res.data;
  },

  deleteBranch: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.BRANCHES}/${id}`);
    return res.data;
  },
};
