import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface StaffAssignData {
  userId: number;
  branchId: number;
  staffCode: string;
  position: string;
}

export const adminUserService = {
  getUsers: async (role?: string) => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.USERS}`, {
      params: role ? { role } : undefined,
    });
    return res.data;
  },

  getUserById: async (id: number) => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.USERS}/${id}`);
    return res.data;
  },

  createStaff: async (data: any) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.USERS}/staff`, data);
    return res.data;
  },

  updateUser: async (id: number, data: { email: string; role: string }) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.USERS}/${id}`, data);
    return res.data;
  },

  toggleUserStatus: async (id: number) => {
    const res = await api.patch(`${ADMIN_API_ENDPOINTS.USERS}/${id}/status`);
    return res.data;
  },

  resetPassword: async (id: number) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.USERS}/${id}/reset-password`);
    return res.data;
  },

  deleteUser: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.USERS}/${id}`);
    return res.data;
  },

  // Phân công chi nhánh nhân sự
  getStaffAssignments: async () => {
    const res = await api.get('/admin/staffs/assignments');
    return res.data;
  },

  assignStaffToBranch: async (data: StaffAssignData) => {
    const res = await api.post('/admin/staffs', data);
    return res.data;
  },
};
