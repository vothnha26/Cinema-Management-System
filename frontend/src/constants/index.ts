export const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082/api';
export const WS_URL = 'http://localhost:8082/ws';

export const USER_ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  STAFF: 'STAFF',
  CUSTOMER: 'CUSTOMER',
} as const;

export const PAYMENT_METHODS = {
  CASH: 'CASH',
  CARD: 'CARD',
  SEPAY: 'SEPAY',
} as const;

export const BOOKING_STATUS = {
  PAID: 'PAID',
  PENDING: 'PENDING',
  CANCELLED: 'CANCELLED',
} as const;

export const SEAT_TYPES = {
  NORMAL: 'NORMAL',
  VIP: 'VIP',
  SWEETBOX: 'SWEETBOX',
} as const;
