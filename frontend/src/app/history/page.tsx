'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Search, QrCode, X, Calendar, Ticket, MapPin } from 'lucide-react';
import { bookingService } from '../../services/bookingService';
import { formatPrice } from '../../utils/format';
import { useAuthStore } from '../../store/authStore';

interface BookingCombo {
  comboId: number;
  comboName: string;
  quantity: number;
  price: number;
}

interface Booking {
  id: number;
  bookingCode: string;
  movieTitle: string;
  branchName: string;
  roomName: string;
  startTime: string;
  format: string;
  seats: string[];
  combos: BookingCombo[];
  totalPrice: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}

type Tab = 'all' | 'PAID' | 'CANCELLED';

export default function HistoryPage() {
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuthStore();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [activeTab, setActiveTab] = useState<Tab>('all');
  const [search, setSearch] = useState('');
  const [qrTicket, setQrTicket] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/auth?message=' + encodeURIComponent('Vui lòng đăng nhập để xem lịch sử đặt vé!'));
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (!isAuthenticated) return;

    const fetchHistory = async () => {
      setLoading(true);
      try {
        const resData = await bookingService.getBookingHistory();
        if (resData) {
          setBookings(resData);
        }
      } catch (err) {
        console.error('Lỗi khi tải lịch sử đặt vé:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [isAuthenticated]);

  const filtered = bookings.filter((b) => {
    if (activeTab !== 'all' && b.status !== activeTab) return false;
    if (search && !b.movieTitle.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  if (loading || authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải lịch sử đặt vé...</div>
      </div>
    );
  }

  const fmt = (n: number) => formatPrice(n);

  return (
    <div className="min-h-screen bg-[#FAFAFA] pt-24 pb-16">
      <div className="max-w-3xl mx-auto px-6">
        <h1 className="text-2xl font-extrabold text-[#22232B] mb-6">Lịch sử đặt vé</h1>

        {/* Filter Bar */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <div className="flex bg-[#F4F4F5] rounded-xl p-1 gap-1">
            {([['all', 'Tất cả'], ['PAID', 'Đã thanh toán'], ['CANCELLED', 'Đã hủy']] as [Tab, string][]).map(([tab, label]) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                  activeTab === tab ? 'bg-white text-[#22232B] shadow-sm' : 'text-[#6B7280]'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
          <div className="flex-1 flex items-center gap-2 bg-white border border-black/10 rounded-xl px-3 py-2">
            <Search className="w-4 h-4 text-[#6B7280]" />
            <input
              placeholder="Tìm theo tên phim..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="flex-1 bg-transparent text-sm outline-none text-[#22232B] placeholder:text-[#6B7280]"
            />
          </div>
        </div>

        {/* Ticket List */}
        <div className="space-y-4">
          {filtered.map((booking) => (
            <div key={booking.id} className="bg-white rounded-2xl border border-black/5 overflow-hidden flex shadow-sm hover:shadow transition-shadow">
              <div className="w-20 bg-[#FEF3DC] flex items-center justify-center font-bold text-2xl text-[#F5A623] shrink-0 select-none">
                🎬
              </div>
              <div className="flex-1 p-4 flex flex-col sm:flex-row gap-3">
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-[#22232B] text-base mb-2 truncate" title={booking.movieTitle}>
                    {booking.movieTitle}
                  </h3>
                  <div className="text-xs text-[#6B7280] space-y-1.5">
                    <div className="flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 shrink-0" />
                      {booking.branchName} · {booking.roomName}
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Calendar className="w-3.5 h-3.5 shrink-0" />
                      {booking.startTime.replace('T', ' ').substring(0, 16)}
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Ticket className="w-3.5 h-3.5 shrink-0" />
                      Ghế: <span className="font-semibold text-[#22232B]">{booking.seats.join(', ')}</span>
                    </div>
                  </div>
                </div>
                <div className="flex flex-col items-start sm:items-end justify-between gap-2 shrink-0">
                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full ${
                      booking.status === 'PAID'
                        ? 'bg-green-50 text-green-600 border border-green-200'
                        : booking.status === 'PENDING'
                        ? 'bg-yellow-50 text-yellow-600 border border-yellow-200'
                        : 'bg-red-50 text-red-500 border border-red-200'
                    }`}
                  >
                    {booking.status === 'PAID'
                      ? 'Đã thanh toán'
                      : booking.status === 'PENDING'
                      ? 'Chờ thanh toán'
                      : 'Đã hủy'}
                  </span>
                  <div className="font-extrabold text-[#22232B] text-sm">{fmt(booking.totalPrice)}</div>
                  {booking.status === 'PAID' && (
                    <button
                      onClick={() => setQrTicket(booking)}
                      className="flex items-center gap-1.5 text-xs font-semibold text-[#F5A623] hover:text-[#C47D0A] transition-colors p-1"
                    >
                      <QrCode className="w-4 h-4" />
                      Xem vé QR
                    </button>
                  )}
                  {booking.status === 'PENDING' && (
                    <button
                      onClick={() => router.push(`/payment/${booking.bookingCode}`)}
                      className="text-xs font-semibold text-white bg-[#F5A623] hover:bg-[#E09415] px-3 py-1.5 rounded-lg transition-colors"
                    >
                      Thanh toán
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}

          {filtered.length === 0 && (
            <div className="text-center py-16 text-[#6B7280] bg-white rounded-2xl border border-black/5">
              <div className="text-4xl mb-3">🎫</div>
              <div className="font-medium text-sm">Chưa có giao dịch đặt vé nào</div>
            </div>
          )}
        </div>
      </div>

      {/* QR Code Ticket Modal */}
      {qrTicket && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl p-6 w-full max-w-xs relative shadow-2xl">
            <button
              onClick={() => setQrTicket(null)}
              className="absolute top-4 right-4 w-8 h-8 rounded-full bg-black/5 flex items-center justify-center hover:bg-black/10 transition-colors"
            >
              <X className="w-4 h-4 text-[#6B7280]" />
            </button>

            <h3 className="font-bold text-[#22232B] text-base mb-4">Vé Điện Tử</h3>

            {/* QR Code soát vé */}
            <div className="bg-[#F4F4F5] rounded-2xl p-4 flex items-center justify-center mb-4">
              <div className="w-40 h-40 bg-white rounded-xl flex items-center justify-center overflow-hidden border border-black/5 shadow-inner">
                <img
                  src={`https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${qrTicket.bookingCode}`}
                  alt="Ticket Soat Ve QR Code"
                  className="w-36 h-36"
                />
              </div>
            </div>

            <div className="text-center mb-4">
              <div className="font-mono text-sm font-bold text-[#F5A623] bg-[#F4F4F5] rounded-lg px-3 py-2 inline-block border border-black/5">
                {qrTicket.bookingCode}
              </div>
            </div>

            <div className="space-y-2 text-xs border-t border-black/5 pt-4">
              <div className="flex justify-between">
                <span className="text-[#6B7280]">Phim</span>
                <span className="font-semibold text-[#22232B] truncate max-w-[150px]">{qrTicket.movieTitle}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6B7280]">Ghế</span>
                <span className="font-semibold text-[#22232B]">{qrTicket.seats.join(', ')}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6B7280]">Rạp chiếu</span>
                <span className="font-semibold text-[#22232B]">{qrTicket.branchName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6B7280]">Phòng</span>
                <span className="font-semibold text-[#22232B]">{qrTicket.roomName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6B7280]">Giờ chiếu</span>
                <span className="font-semibold text-[#22232B]">{qrTicket.startTime.replace('T', ' ').substring(0, 16)}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
