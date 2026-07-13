import api from '../config/api';

export const authService = {
  /**
   * Đăng nhập người dùng
   */
  async login(payload: any) {
    // Spring Boot AuthController nhận login qua { username, password }
    const response = await api.post('/auth/login', payload);
    return response.data;
  },

  /**
   * Đăng ký người dùng mới
   */
  async register(payload: any) {
    const response = await api.post('/auth/register', payload);
    return response.data;
  },

  /**
   * Cập nhật thông tin cá nhân
   */
  async updateProfile(payload: any) {
    const response = await api.put('/users/profile', payload);
    return response.data;
  },

  /**
   * Đổi mật khẩu tài khoản
   */
  async changePassword(payload: any) {
    const response = await api.put('/users/change-password', payload);
    return response.data;
  },

  /**
   * Xác thực mã OTP
   */
  async verifyOtp(payload: { email: string; otp: string }) {
    const response = await api.post('/auth/verify-otp', payload);
    return response.data;
  },

  /**
   * Yêu cầu gửi mã OTP quên mật khẩu
   */
  async forgotPassword(email: string) {
    const response = await api.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
    return response.data;
  },

  /**
   * Đặt lại mật khẩu mới bằng token (mã OTP)
   */
  async resetPassword(payload: any) {
    const response = await api.post(`/auth/reset-password?token=${encodeURIComponent(payload.token)}&newPassword=${encodeURIComponent(payload.newPassword)}`);
    return response.data;
  },
};
