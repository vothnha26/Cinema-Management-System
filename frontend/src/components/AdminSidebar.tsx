'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  LayoutDashboard,
  Film,
  Calendar,
  Armchair,
  DollarSign,
  Receipt,
  Coffee,
  MapPin,
  Award,
  BadgePercent,
  Users,
  History,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Share2,
} from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { ADMIN_ROUTES } from '../constants/admin';

export default function AdminSidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const [mounted, setMounted] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuthStore();

  useEffect(() => {
    setMounted(true);
  }, []);

  const handleLogout = () => {
    logout();
    router.push('/auth');
  };

  // Cấu hình danh sách menu
  const menuItems = [
    {
      label: 'Tổng quan',
      path: ADMIN_ROUTES.DASHBOARD,
      icon: LayoutDashboard,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Quản lý Phim',
      path: ADMIN_ROUTES.MOVIES,
      icon: Film,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Phân bổ Phim',
      path: ADMIN_ROUTES.DISTRIBUTIONS,
      icon: Share2,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Suất chiếu',
      path: ADMIN_ROUTES.SHOWTIMES,
      icon: Calendar,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Phòng & Ghế',
      path: ADMIN_ROUTES.ROOMS,
      icon: Armchair,
      roles: ['ADMIN'],
    },
    {
      label: 'Ma trận Giá',
      path: ADMIN_ROUTES.PRICING,
      icon: DollarSign,
      roles: ['ADMIN'],
    },
    {
      label: 'Đặt vé & Check-in',
      path: ADMIN_ROUTES.BOOKINGS,
      icon: Receipt,
      roles: ['ADMIN', 'MANAGER', 'STAFF'],
    },
    {
      label: 'Combo bắp nước',
      path: ADMIN_ROUTES.COMBOS,
      icon: Coffee,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Chi nhánh rạp',
      path: ADMIN_ROUTES.BRANCHES,
      icon: MapPin,
      roles: ['ADMIN', 'MANAGER'],
    },
    {
      label: 'Hạng thành viên',
      path: ADMIN_ROUTES.MEMBERSHIP,
      icon: Award,
      roles: ['ADMIN'],
    },
    {
      label: 'Mã giảm giá',
      path: ADMIN_ROUTES.PROMOTIONS,
      icon: BadgePercent,
      roles: ['ADMIN'],
    },
    {
      label: 'Nhân sự',
      path: ADMIN_ROUTES.USERS,
      icon: Users,
      roles: ['ADMIN'],
    },
    {
      label: 'Nhật ký hệ thống',
      path: ADMIN_ROUTES.AUDIT,
      icon: History,
      roles: ['ADMIN'],
    },
  ];

  // Lọc menu theo vai trò người dùng (Zustand store)
  const userRole = user?.role || 'STAFF';
  const filteredMenu = menuItems.filter((item) => item.roles.includes(userRole));

  return (
    <div
      className={`min-h-screen bg-[#1C1C22] text-[#FAFAFA] flex flex-col justify-between border-r border-white/5 transition-all duration-300 ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      <div>
        {/* Logo header */}
        <div className="h-16 flex items-center justify-between px-5 border-b border-white/5">
          <Link href="/" className="flex items-center gap-2 overflow-hidden">
            <div className="w-8 h-8 rounded-lg bg-[#F5A623] flex items-center justify-center shrink-0">
              <Film className="w-4 h-4 text-white" />
            </div>
            {!collapsed && <span className="font-bold text-lg text-white">StarCinema</span>}
          </Link>
          <button
            onClick={() => setCollapsed(!collapsed)}
            className="w-7 h-7 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center transition-colors"
          >
            {collapsed ? <ChevronRight className="w-4 h-4 text-[#FAFAFA]" /> : <ChevronLeft className="w-4 h-4 text-[#FAFAFA]" />}
          </button>
        </div>

        {/* Menu Navigation */}
        <nav className="p-3 space-y-1">
          {mounted ? (
            filteredMenu.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.path;
              return (
                <Link
                  key={item.path}
                  href={item.path}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                    isActive
                      ? 'bg-[#F5A623] text-white font-bold shadow-lg shadow-[#F5A623]/25'
                      : 'text-[#6B7280] hover:text-[#FAFAFA] hover:bg-white/5'
                  }`}
                >
                  <Icon className="w-5 h-5 shrink-0" />
                  {!collapsed && <span className="text-sm">{item.label}</span>}
                </Link>
              );
            })
          ) : (
            Array.from({ length: menuItems.length }).map((_, i) => (
              <div key={i} className="flex items-center gap-3 px-4 py-3 rounded-xl animate-pulse bg-white/5">
                <div className="w-5 h-5 rounded-md bg-white/10 shrink-0" />
                {!collapsed && <div className="h-4 bg-white/10 rounded w-2/3" />}
              </div>
            ))
          )}
        </nav>
      </div>

      {/* Footer logout */}
      <div className="p-3 border-t border-white/5">
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-[#EF4444] hover:bg-red-500/10 transition-all font-semibold"
        >
          <LogOut className="w-5 h-5 shrink-0" />
          {!collapsed && <span className="text-sm">Đăng xuất</span>}
        </button>
      </div>
    </div>
  );
}
