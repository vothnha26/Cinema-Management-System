import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: đính kèm JWT token nếu có
api.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: xử lý logout khi token hết hạn (401)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      if (typeof window !== 'undefined') {
        const authState = useAuthStore.getState();
        // Guard chống loop: Chỉ kích hoạt logout/redirect nếu trạng thái hiện tại là đã đăng nhập
        if (authState.isAuthenticated) {
          authState.logout();
          
          // Tránh loop redirect nếu đã ở trang đăng nhập
          if (!window.location.pathname.startsWith('/auth')) {
            window.location.href = `/auth?message=${encodeURIComponent('Phiên đăng nhập hết hạn')}`;
          }
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
