import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface PromotionData {
  id?: number;
  code: string;
  description: string;
  discountType: 'PERCENT' | 'FLAT';
  discountValue: number;
  minOrderAmount: number;
  maxDiscountAmount?: number;
  startDate: string; // ISO Date String
  endDate: string; // ISO Date String
  usageLimit: number;
  isActive: boolean;
}

export const adminPromotionService = {
  getPromotions: async () => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.PROMOTIONS}`);
    return res.data;
  },

  createPromotion: async (data: PromotionData) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.PROMOTIONS}`, data);
    return res.data;
  },

  updatePromotion: async (id: number, data: PromotionData) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.PROMOTIONS}/${id}`, data);
    return res.data;
  },

  deletePromotion: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.PROMOTIONS}/${id}`);
    return res.data;
  },
};
