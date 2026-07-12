import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface MembershipLevel {
  id: number;
  name: string; // STANDARD, SILVER, GOLD, PLATINUM
}

export interface MembershipBenefitData {
  id: number;
  membershipLevel: MembershipLevel;
  benefitType: 'DISCOUNT' | 'POINT_MULTIPLIER';
  benefitValue: string;
}

export interface CustomerData {
  id: number;
  fullName: string;
  phone?: string;
  email?: string;
  membershipLevel?: MembershipLevel;
  points: number;
  totalSpending: number;
}

export interface UpdateCustomerPayload {
  fullName: string;
  phone?: string;
  email?: string;
  membershipLevelName: string; // STANDARD, SILVER, GOLD, PLATINUM
  points: number;
  totalSpending: number;
}

export const adminMembershipService = {
  getMembershipBenefits: async () => {
    const res = await api.get(ADMIN_API_ENDPOINTS.MEMBERSHIP_BENEFITS);
    return res.data;
  },

  updateBenefit: async (levelName: string, type: 'DISCOUNT' | 'POINT_MULTIPLIER', value: number) => {
    // API backend cũ nhận request params dạng RPC
    const res = await api.post(
      `${ADMIN_API_ENDPOINTS.MEMBERSHIP_BENEFITS}/update`,
      {},
      {
        params: {
          levelName,
          type,
          value,
        },
      }
    );
    return res.data;
  },

  getCustomers: async () => {
    const res = await api.get(ADMIN_API_ENDPOINTS.CUSTOMERS);
    return res.data;
  },

  updateCustomer: async (id: number, data: UpdateCustomerPayload) => {
    const res = await api.put(`${ADMIN_API_ENDPOINTS.CUSTOMERS}/${id}`, data);
    return res.data;
  },
};
