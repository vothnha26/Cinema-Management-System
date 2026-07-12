import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface ComboData {
  id?: number;
  name: string;
  description: string;
  price: number;
  isActive: boolean;
}

export const adminComboService = {
  getCombos: async () => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.COMBOS}`);
    return res.data;
  },

  getCombosByBranch: async (branchId: number) => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.COMBOS}/branch/${branchId}`);
    return res.data;
  },

  createCombo: async (data: ComboData, imageFile?: File) => {
    const formData = new FormData();
    formData.append(
      'combo',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );
    if (imageFile) {
      formData.append('image', imageFile);
    }
    const res = await api.post(`${ADMIN_API_ENDPOINTS.COMBOS}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  updateCombo: async (id: number, data: ComboData, imageFile?: File) => {
    const formData = new FormData();
    formData.append(
      'combo',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );
    if (imageFile) {
      formData.append('image', imageFile);
    }
    const res = await api.put(`${ADMIN_API_ENDPOINTS.COMBOS}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  updateStock: async (id: number, quantity: number) => {
    const res = await api.patch(`${ADMIN_API_ENDPOINTS.COMBOS}/${id}/stock`, null, {
      params: { quantity },
    });
    return res.data;
  },

  updateBranchStock: async (branchId: number, comboId: number, quantity: number) => {
    const res = await api.patch(
      `${ADMIN_API_ENDPOINTS.COMBOS}/branch/${branchId}/combo/${comboId}/stock`,
      null,
      { params: { quantity } }
    );
    return res.data;
  },

  toggleBranchActive: async (branchId: number, comboId: number, active: boolean) => {
    const res = await api.patch(
      `${ADMIN_API_ENDPOINTS.COMBOS}/branch/${branchId}/combo/${comboId}/toggle-active`,
      null,
      { params: { active } }
    );
    return res.data;
  },

  deleteCombo: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.COMBOS}/${id}`);
    return res.data;
  },
};
