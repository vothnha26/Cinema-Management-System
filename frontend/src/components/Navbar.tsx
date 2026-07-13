'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Search, Bell, ChevronDown, Film, X, Menu, LogOut, User as UserIcon, History } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { USER_ROLES } from '../constants';

export default function Navbar() {
  const [searchOpen, setSearchOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [mounted, setMounted] = useState(false);
  const pathname = usePathname();

  const { isAuthenticated, user, logout } = useAuthStore();

  useEffect(() => {
    setMounted(true);
  }, []);

  const links = [
    { label: 'Trang chủ', path: '/' },
    { label: 'Phim đang chiếu', path: '/#now-playing' },
    { label: 'Sắp chiếu', path: '/#upcoming' },
  ];

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(-2)
      .join('')
      .toUpperCase();
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-black/5">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2 shrink-0">
          <div className="w-8 h-8 rounded-lg bg-[#F5A623] flex items-center justify-center">
            <Film className="w-4 h-4 text-white" />
          </div>
          <span className="font-bold text-lg text-[#22232B] tracking-tight">StarCinema</span>
        </Link>

        {/* Desktop links */}
        <div className="hidden md:flex items-center gap-1">
          {links.map((l) => (
            <Link
              key={l.label}
              href={l.path}
              className={`px-3 py-2 text-sm font-medium rounded-lg transition-all ${
                pathname === l.path
                  ? 'text-[#F5A623] bg-[#FEF3DC]'
                  : 'text-[#6B7280] hover:text-[#22232B] hover:bg-black/5'
              }`}
            >
              {l.label}
            </Link>
          ))}
        </div>

        {/* Right actions */}
        <div className="flex items-center gap-2 relative">
          {searchOpen ? (
            <div className="flex items-center gap-2 bg-[#F4F4F5] rounded-xl px-3 py-2">
              <Search className="w-4 h-4 text-[#6B7280]" />
              <input
                autoFocus
                placeholder="Tìm phim..."
                className="bg-transparent text-sm outline-none w-40 text-[#22232B] placeholder:text-[#6B7280]"
              />
              <button onClick={() => setSearchOpen(false)}>
                <X className="w-4 h-4 text-[#6B7280] hover:text-[#22232B]" />
              </button>
            </div>
          ) : (
            <button
              onClick={() => setSearchOpen(true)}
              className="w-9 h-9 flex items-center justify-center rounded-xl hover:bg-black/5 transition-colors text-[#6B7280] hover:text-[#22232B]"
            >
              <Search className="w-4 h-4" />
            </button>
          )}

          {mounted ? (
            isAuthenticated && user ? (
              <>
                <button className="w-9 h-9 flex items-center justify-center rounded-xl hover:bg-black/5 transition-colors text-[#6B7280] hover:text-[#22232B] relative">
                  <Bell className="w-4 h-4" />
                  <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-[#F5A623] rounded-full" />
                </button>
                
                <div className="relative">
                  <button
                    onClick={() => setDropdownOpen(!dropdownOpen)}
                    className="flex items-center gap-2 px-3 py-1.5 rounded-xl hover:bg-black/5 transition-colors"
                  >
                    <div className="w-7 h-7 rounded-full bg-[#F5A623]/20 flex items-center justify-center text-xs font-bold text-[#F5A623]">
                      {getInitials(user.fullName)}
                    </div>
                    <span className="hidden sm:block text-sm font-medium text-[#22232B]">{user.fullName}</span>
                    <ChevronDown className="w-3 h-3 text-[#6B7280]" />
                  </button>

                  {/* Dropdown Menu */}
                  {dropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white border border-black/5 rounded-2xl shadow-xl py-2 z-50">
                      {user.role !== USER_ROLES.CUSTOMER && (
                        <Link
                          href={user.role === USER_ROLES.STAFF ? '/pos' : '/admin'}
                          onClick={() => setDropdownOpen(false)}
                          className="flex items-center gap-2 px-4 py-2 text-sm text-[#22232B] hover:bg-black/5 transition-colors"
                        >
                          <Film className="w-4 h-4" />
                          <span>Trang quản trị</span>
                        </Link>
                      )}
                      <Link
                        href="/profile"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-[#22232B] hover:bg-black/5 transition-colors"
                      >
                        <UserIcon className="w-4 h-4" />
                        <span>Thông tin tài khoản</span>
                      </Link>
                      <Link
                        href="/history"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-[#22232B] hover:bg-black/5 transition-colors"
                      >
                        <History className="w-4 h-4" />
                        <span>Lịch sử mua vé</span>
                      </Link>
                      <hr className="my-1 border-black/5" />
                      <button
                        onClick={() => {
                          setDropdownOpen(false);
                          logout();
                        }}
                        className="w-full flex items-center gap-2 px-4 py-2 text-sm text-[#C0392B] hover:bg-red-50 transition-colors text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Đăng xuất</span>
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link
                href="/auth"
                className="px-4 py-2 rounded-xl bg-[#F5A623] text-white text-sm font-semibold hover:bg-[#E09415] transition-colors"
              >
                Đăng nhập
              </Link>
            )
          ) : (
            <div className="flex items-center gap-2 animate-pulse">
              <div className="w-9 h-9 rounded-xl bg-black/5" />
              <div className="w-24 h-9 rounded-xl bg-black/5" />
            </div>
          )}

          <button
            className="md:hidden w-9 h-9 flex items-center justify-center rounded-xl hover:bg-black/5 transition-colors"
            onClick={() => setMobileOpen(!mobileOpen)}
          >
            <Menu className="w-4 h-4 text-[#6B7280]" />
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden bg-white border-t border-black/5 px-4 py-3 flex flex-col gap-1">
          {links.map((l) => (
            <Link
              key={l.label}
              href={l.path}
              onClick={() => setMobileOpen(false)}
              className="text-left px-3 py-2 text-sm font-medium text-[#6B7280] hover:text-[#22232B] rounded-lg hover:bg-black/5 transition-all"
            >
              {l.label}
            </Link>
          ))}
        </div>
      )}
    </nav>
  );
}
