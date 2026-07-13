'use client';

import { useState, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { Bell, User } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { USER_ROLES } from '../constants';

export default function AdminTopbar() {
  const pathname = usePathname();
  const { user } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Tạo breadcrumbs đơn giản
  const getBreadcrumbs = () => {
    const parts = pathname.split('/').filter(Boolean);
    return parts.map((part, index) => {
      const isLast = index === parts.length - 1;
      const label =
        part === 'admin'
          ? 'Quản trị'
          : part === 'movies'
          ? 'Quản lý Phim'
          : part === 'showtimes'
          ? 'Suất chiếu'
          : part === 'rooms'
          ? 'Phòng & Ghế'
          : part === 'pricing'
          ? 'Ma trận Giá'
          : part === 'bookings'
          ? 'Đặt vé & Check-in'
          : part === 'combos'
          ? 'Combos'
          : part === 'branches'
          ? 'Chi nhánh'
          : part === 'membership'
          ? 'Hạng thành viên'
          : part === 'promotions'
          ? 'Mã giảm giá'
          : part === 'users'
          ? 'Nhân sự'
          : part === 'audit'
          ? 'Nhật ký'
          : part;

      return (
        <span key={part} className="flex items-center gap-1 text-sm">
          {index > 0 && <span className="text-[#6B7280]">/</span>}
          <span className={isLast ? 'font-bold text-[#22232B]' : 'text-[#6B7280]'}>{label}</span>
        </span>
      );
    });
  };

  return (
    <header className="h-16 bg-white border-b border-black/5 flex items-center justify-between px-6 shrink-0">
      {/* Breadcrumbs */}
      <div className="flex items-center gap-1">{getBreadcrumbs()}</div>

      {/* User Actions */}
      <div className="flex items-center gap-4">
        {/* Notifications Icon */}
        <button className="w-9 h-9 rounded-xl hover:bg-[#FAFAFA] transition-colors flex items-center justify-center text-[#6B7280]">
          <Bell className="w-5 h-5" />
        </button>

        {/* User profile dropdown info */}
        <div className="flex items-center gap-3 border-l border-black/5 pl-4">
          {mounted ? (
            <>
              <div className="text-right">
                <div className="text-sm font-bold text-[#22232B]">{user?.fullName || 'Quản trị viên'}</div>
                <div className="text-xs text-[#6B7280]">{user?.role || USER_ROLES.ADMIN}</div>
              </div>
              <div className="w-9 h-9 rounded-xl bg-[#F5A623]/10 text-[#F5A623] flex items-center justify-center font-bold">
                <User className="w-5 h-5" />
              </div>
            </>
          ) : (
            <div className="flex items-center gap-3 animate-pulse">
              <div className="text-right space-y-1">
                <div className="w-20 h-4 bg-black/5 rounded" />
                <div className="w-12 h-3 bg-black/5 rounded ml-auto" />
              </div>
              <div className="w-9 h-9 rounded-xl bg-black/5" />
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
