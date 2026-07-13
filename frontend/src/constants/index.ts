export const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082/api';

// getWsUrl: Helper để lấy WebSocket URL động tùy thuộc môi trường và protocol (chuyển sang wss khi chạy HTTPS)
// Fallback 'ws://localhost:8082/ws-cinema/websocket' chỉ dùng làm giá trị mặc định lúc SSR để tránh throw lỗi khi build.
export const getWsUrl = () => {
  if (process.env.NEXT_PUBLIC_WS_URL) return process.env.NEXT_PUBLIC_WS_URL;
  if (typeof window === 'undefined') return 'ws://localhost:8082/ws-cinema/websocket';
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.hostname}:8082/ws-cinema/websocket`;
};

// Cấu hình chế độ chạy Demo để giả lập tự động thanh toán khi cần kiểm thử cục bộ.
// Mặc định false để đảm bảo an toàn bảo mật khi deploy (fail-closed).
export const DEMO_MODE = process.env.NEXT_PUBLIC_DEMO_MODE === 'true';
export const DEMO_AUTO_PAID_TIMEOUT_MS = 18000;

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
