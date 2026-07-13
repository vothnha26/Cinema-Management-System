import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface ConditionData {
  type: string; // ví dụ: SEAT_TYPE, ROOM_TYPE, DAY_OF_WEEK, SHOW_TIME
  value: string; // ví dụ: VIP, 3D, WEEKEND, LATE_SHOW
  description?: string;
}

export interface PricingRuleData {
  id?: number;
  name: string;
  description?: string;
  category: 'BASE' | 'SURCHARGE' | 'DISCOUNT';
  impactType: 'ADDITIVE' | 'SUBTRACTIVE' | 'PERCENTAGE' | 'FIXED';
  impactValue: number;
  priority: number;
  active?: boolean;
  stackable?: boolean;
  systemRule?: boolean;
  maxDiscountLimit?: number;
  conditions?: ConditionData[];
}

export const adminPricingService = {
  getRules: async (branchId?: number) => {
    const res = await api.get(`${ADMIN_API_ENDPOINTS.PRICING_RULES}`, {
      params: branchId ? { branchId } : undefined,
    });
    return res.data;
  },

  createRule: async (data: PricingRuleData) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.PRICING_RULES}`, data);
    return res.data;
  },

  updateRule: async (id: number, data: PricingRuleData) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.PRICING_RULES}/${id}`, data);
    return res.data;
  },

  deleteRule: async (id: number) => {
    const res = await api.delete(`${ADMIN_API_ENDPOINTS.PRICING_RULES}/${id}`);
    return res.data;
  },

  reorderRules: async (ruleIds: number[], branchId?: number) => {
    const res = await api.post(`${ADMIN_API_ENDPOINTS.PRICING_RULES}/reorder`, ruleIds, {
      params: branchId ? { branchId } : undefined,
    });
    return res.data;
  },
};
