import { USER_ROLES } from './index';

export const ADMIN_ROUTES = {
  DASHBOARD: '/admin',
  MOVIES: '/admin/movies',
  SHOWTIMES: '/admin/showtimes',
  ROOMS: '/admin/rooms',
  PRICING: '/admin/pricing',
  BOOKINGS: '/admin/bookings',
  COMBOS: '/admin/combos',
  BRANCHES: '/admin/branches',
  MEMBERSHIP: '/admin/membership',
  PROMOTIONS: '/admin/promotions',
  USERS: '/admin/users',
  AUDIT: '/admin/audit',
} as const;

export const ADMIN_API_ENDPOINTS = {
  DASHBOARD_STATS: '/admin/dashboard/stats',
  MOVIES: '/admin/movies',
  SHOWTIMES: '/admin/showtimes',
  ROOMS: '/admin/rooms',
  PRICING: '/admin/pricing',
  BOOKINGS: '/admin/bookings',
  COMBOS: '/admin/combos',
  BRANCHES: '/admin/branches',
  USERS: '/admin/users',
  PROMOTIONS: '/admin/promotions',
  MEMBERSHIP: '/admin/memberships',
  AUDIT_LOGS: '/admin/audit-logs',
} as const;

// Hằng số phân quyền các module cho từng vai trò admin
export const MODULE_PERMISSIONS = {
  [USER_ROLES.ADMIN]: Object.values(ADMIN_ROUTES),
  [USER_ROLES.MANAGER]: [
    ADMIN_ROUTES.DASHBOARD,
    ADMIN_ROUTES.MOVIES,
    ADMIN_ROUTES.SHOWTIMES,
    ADMIN_ROUTES.COMBOS,
    ADMIN_ROUTES.BRANCHES,
  ],
  [USER_ROLES.STAFF]: [
    ADMIN_ROUTES.BOOKINGS,
  ],
} as const;
