'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { User, Ticket, Lock, LogOut, Star, TrendingUp } from 'lucide-react';
import { customerService } from '../../services/customerService';
import { useAuthStore } from '../../store/authStore';

export default function ProfilePage() {
  const router = useRouter();
  const { user, logout, isAuthenticated, loading: authLoading, updateUser } = useAuthStore();

  const [activeMenu, setActiveMenu] = useState('info');
  
  // Trạng thái edit profile
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [birthday, setBirthday] = useState('');
  const [email, setEmail] = useState('');
  
  // Trạng thái đổi mật khẩu
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/auth?message=' + encodeURIComponent('Vui lòng đăng nhập để xem thông tin cá nhân!'));
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setPhone(user.phoneNumber || '');
      setEmail(user.email || '');
      if (user.birthday) {
        setBirthday(user.birthday.split('T')[0]);
      }
    }
  }, [user]);

  const handleMenu = (key: string) => {
    if (key === 'logout') {
      logout();
      router.push('/');
      return;
    }
    if (key === 'history') {
      router.push('/history');
      return;
    }
    setActiveMenu(key);
    setSuccessMsg('');
    setErrorMsg('');
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg('');
    setErrorMsg('');
    setLoading(true);

    try {
      const resData = await customerService.updateProfile({
        fullName,
        phoneNumber: phone,
        birthday: birthday ? `${birthday}T00:00:00` : null,
      });

      if (resData) {
        // Cập nhật lại thông tin user trong Zustand store
        updateUser(resData);
        setSuccessMsg('Cập nhật thông tin cá nhân thành công!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Cập nhật thất bại. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg('');
    setErrorMsg('');

    if (newPassword !== confirmPassword) {
      setErrorMsg('Mật khẩu mới xác nhận không khớp!');
      return;
    }

    setLoading(true);
    try {
      await customerService.changePassword({
        oldPassword,
        newPassword,
      });
      setSuccessMsg('Đổi mật khẩu thành công!');
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Mật khẩu cũ không chính xác!');
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải hồ sơ...</div>
      </div>
    );
  }

  const getInitials = (name: string) => {
    if (!name) return 'U';
    const words = name.trim().split(' ');
    if (words.length > 1) {
      return (words[0][0] + words[words.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const menuItems = [
    { icon: User, label: 'Thông tin cá nhân', key: 'info' },
    { icon: Ticket, label: 'Lịch sử đặt vé', key: 'history' },
    { icon: Lock, label: 'Đổi mật khẩu', key: 'password' },
    { icon: LogOut, label: 'Đăng xuất', key: 'logout' },
  ];

  return (
    <div className="min-h-screen bg-[#F4F4F5] pt-24 pb-16">
      <div className="max-w-5xl mx-auto px-6 flex flex-col md:flex-row gap-6">
        {/* Sidebar */}
        <div className="w-full md:w-64 shrink-0">
          <div className="bg-white rounded-2xl p-6 border border-black/5 shadow-sm">
            {/* User card summary */}
            <div className="text-center mb-6">
              <div className="w-20 h-20 rounded-full bg-[#F5A623]/15 flex items-center justify-center text-2xl font-extrabold text-[#F5A623] mx-auto mb-3 border border-[#F5A623]/25">
                {getInitials(user.fullName)}
              </div>
              <div className="font-bold text-[#22232B]">{user.fullName}</div>
              <div className="text-xs text-[#6B7280] mb-2 truncate" title={user.email}>{user.email}</div>
              <div className="inline-flex items-center gap-1 bg-[#FEF3DC] text-[#C47D0A] text-xs font-semibold px-3 py-1 rounded-full">
                <Star className="w-3 h-3 fill-current" />
                Thành viên StarCinema
              </div>
            </div>

            <nav className="space-y-1">
              {menuItems.map((item) => {
                const Icon = item.icon;
                const active = activeMenu === item.key;
                return (
                  <button
                    key={item.key}
                    onClick={() => handleMenu(item.key)}
                    className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                      active
                        ? 'bg-[#FEF3DC] text-[#C47D0A]'
                        : item.key === 'logout'
                        ? 'text-[#C0392B] hover:bg-red-50'
                        : 'text-[#6B7280] hover:text-[#22232B] hover:bg-black/5'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    {item.label}
                  </button>
                );
              })}
            </nav>
          </div>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 space-y-5">
          {successMsg && (
            <div className="bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl">
              {successMsg}
            </div>
          )}

          {errorMsg && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
              {errorMsg}
            </div>
          )}

          {activeMenu === 'info' && (
            <>
              {/* Stat dashboard cards */}
              <div className="grid grid-cols-3 gap-4">
                {[
                  { label: 'Hạng thành viên', value: 'Bạc', icon: Star },
                  { label: 'Vai trò', value: user.role === 'ADMIN' ? 'Admin' : user.role === 'STAFF' ? 'Nhân viên' : 'Khách hàng', icon: User },
                  { label: 'Điểm tích lũy', value: '150 điểm', icon: TrendingUp },
                ].map((stat) => {
                  const Icon = stat.icon;
                  return (
                    <div key={stat.label} className="bg-white rounded-2xl p-4 border border-black/5 text-center shadow-sm">
                      <Icon className="w-5 h-5 text-[#F5A623] mx-auto mb-2" />
                      <div className="text-base font-extrabold text-[#22232B] truncate">{stat.value}</div>
                      <div className="text-[10px] text-[#6B7280] uppercase tracking-wider mt-0.5">{stat.label}</div>
                    </div>
                  );
                })}
              </div>

              {/* Personal Info Form */}
              <div className="bg-white rounded-2xl p-6 border border-black/5 shadow-sm">
                <h3 className="font-bold text-[#22232B] mb-5">Thông tin cá nhân</h3>
                <form onSubmit={handleUpdateProfile} className="space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">
                        Họ và tên
                      </label>
                      <input
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm text-[#22232B] outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">
                        Địa chỉ Email (Không được đổi)
                      </label>
                      <input
                        type="email"
                        disabled
                        value={email}
                        className="w-full px-4 py-3 rounded-xl border border-black/10 bg-black/5 text-sm text-[#6B7280] outline-none cursor-not-allowed"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">
                        Số điện thoại
                      </label>
                      <input
                        type="tel"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        placeholder="Chưa cập nhật"
                        className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm text-[#22232B] outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">
                        Ngày sinh
                      </label>
                      <input
                        type="date"
                        value={birthday}
                        onChange={(e) => setBirthday(e.target.value)}
                        className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm text-[#22232B] outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                      />
                    </div>
                  </div>
                  <button
                    type="submit"
                    disabled={loading}
                    className="px-6 py-3 rounded-xl bg-[#F5A623] text-white text-sm font-bold hover:bg-[#E09415] transition-all disabled:opacity-50"
                  >
                    {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
                  </button>
                </form>
              </div>
            </>
          )}

          {activeMenu === 'password' && (
            <div className="bg-white rounded-2xl p-6 border border-black/5 shadow-sm">
              <h3 className="font-bold text-[#22232B] mb-5">Đổi mật khẩu bảo mật</h3>
              <form onSubmit={handleChangePassword} className="space-y-4 max-w-sm">
                <div>
                  <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">Mật khẩu hiện tại</label>
                  <input
                    type="password"
                    required
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">Mật khẩu mới</label>
                  <input
                    type="password"
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[#6B7280] mb-1.5 uppercase tracking-wide">Xác nhận mật khẩu mới</label>
                  <input
                    type="password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 focus:bg-white transition-all"
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-3 rounded-xl bg-[#F5A623] text-white text-sm font-bold hover:bg-[#E09415] transition-all disabled:opacity-50"
                >
                  {loading ? 'Đang xử lý...' : 'Cập nhật mật khẩu'}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
