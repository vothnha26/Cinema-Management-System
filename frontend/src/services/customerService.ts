import api from '../config/api';

export const customerService = {
  /**
   * Tìm kiếm khách hàng/hội viên theo số điện thoại (cho quầy POS)
   */
  async searchCustomerByPhone(phone: string) {
    const response = await api.get(`/public/customers/search?phone=${encodeURIComponent(phone)}`);
    return response.data;
  },

  /**
   * Cập nhật thông tin cá nhân của người dùng hiện tại
   */
  async updateProfile(payload: { fullName: string; phoneNumber: string; birthday: string | null }) {
    const response = await api.put('/users/profile', payload);
    return response.data;
  },

  /**
   * Đổi mật khẩu tài khoản
   */
  async changePassword(payload: { oldPassword: string; newPassword: string }) {
    const response = await api.put('/users/change-password', payload);
    return response.data;
  },
};
