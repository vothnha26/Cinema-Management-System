import { create } from 'zustand';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'MANAGER' | 'STAFF' | 'CUSTOMER';
  avatar?: string;
  phoneNumber?: string;
  birthday?: string;
}

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>((set) => {
  // Trạng thái ban đầu lấy từ localStorage (nếu đang ở phía Client)
  const getInitialState = () => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr) as User;
          return { token, user, isAuthenticated: true, loading: false };
        } catch {
          // Lỗi parse
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
    }
    return { token: null, user: null, isAuthenticated: false, loading: false };
  };

  return {
    ...getInitialState(),

    login: (token, user) => {
      if (typeof window !== 'undefined') {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        // Thiết lập cookie auth_token để Middleware có thể đọc được trên Server side
        document.cookie = `auth_token=${token}; path=/; max-age=86400; SameSite=Lax`;
      }
      set({ token, user, isAuthenticated: true, loading: false });
    },

    logout: () => {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // Xóa cookie auth_token khi đăng xuất
        document.cookie = 'auth_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax';
      }
      set({ token: null, user: null, isAuthenticated: false, loading: false });
    },

    updateUser: (updatedFields) => {
      set((state) => {
        if (!state.user) return state;
        const newUser = { ...state.user, ...updatedFields };
        if (typeof window !== 'undefined') {
          localStorage.setItem('user', JSON.stringify(newUser));
        }
        return { user: newUser };
      });
    },

    setLoading: (loading) => set({ loading }),
  };
});
