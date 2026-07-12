import api from '../../config/api';
import { ADMIN_API_ENDPOINTS } from '../../constants/admin';

export interface AuditLogData {
  id: number;
  timestamp: string;
  username: string;
  action: string;
  target: string;
  detail: string;
}

export const adminAuditService = {
  getAuditLogs: async () => {
    const res = await api.get(ADMIN_API_ENDPOINTS.AUDIT_LOGS);
    return res.data;
  },
};
