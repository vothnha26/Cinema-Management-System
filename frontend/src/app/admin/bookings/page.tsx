'use client';

import { useState, useEffect } from 'react';
import { Ticket, Search, RefreshCw, Check, AlertCircle, Calendar, User, ShoppingBag } from 'lucide-react';
import { adminBookingService, BookingData } from '../../../services/admin';

export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState<BookingData[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  const fetchBookings = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminBookingService.getAllBookings();
      if (res?.success) {
        setBookings(res.data || []);
      } else {
        setErrorMsg(res?.message || 'Không thể lấy danh sách vé đặt.');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleCheckIn = async (code: string) => {
    if (!confirm(`Xác nhận soát vé cho khách hàng có mã đặt vé: ${code}?`)) return;
    try {
      const res = await adminBookingService.checkInBooking(code);
      if (res?.success) {
        alert('Soát vé thành công!');
        fetchBookings();
      } else {
        alert(res?.message || 'Soát vé thất bại.');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi xảy ra khi soát vé.');
    }
  };

  const filteredBookings = bookings.filter((b) => {
    const q = searchTerm.toLowerCase();
    const codeMatch = b.bookingCode.toLowerCase().includes(q);
    const phoneMatch = b.customerPhone ? b.customerPhone.includes(q) : false;
    const nameMatch = b.customerName ? b.customerName.toLowerCase().includes(q) : false;
    return codeMatch || phoneMatch || nameMatch;
  });

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Ticket className="w-6 h-6 text-[#F5A623]" />
            Quản lý đặt vé & Soát vé
          </h1>
          <p className="text-sm text-[#6B7280]">
            Tra cứu lượt đặt vé của khách hàng, theo dõi bắp nước đi kèm và thực hiện soát vé tại rạp
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={fetchBookings}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="relative max-w-md">
        <Search className="w-4.5 h-4.5 absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
        <input
          type="text"
          placeholder="Tìm kiếm theo mã đặt vé, số điện thoại, tên khách..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] placeholder:text-[#6B7280]/60 font-semibold"
        />
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Table grid */}
      <div className="bg-white border border-black/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#FAFAFA] border-b border-black/5 text-xs font-bold text-[#6B7280] uppercase tracking-wider">
                <th className="p-4 pl-6">Mã Vé</th>
                <th className="p-4">Khách Hàng</th>
                <th className="p-4">Suất Chiếu</th>
                <th className="p-4">Chi Tiết Ghế & Combo</th>
                <th className="p-4">Tổng Tiền</th>
                <th className="p-4">Trạng Thái</th>
                <th className="p-4 pr-6 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-black/5 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-[#6B7280] font-semibold">
                    Đang tải danh sách đặt vé...
                  </td>
                </tr>
              ) : filteredBookings.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-[#6B7280] font-semibold">
                    Không tìm thấy lượt đặt vé nào.
                  </td>
                </tr>
              ) : (
                filteredBookings.map((b) => {
                  const statusMap = {
                    PENDING: { text: 'Chờ TT', cls: 'bg-amber-50 text-amber-700 border-amber-200' },
                    CONFIRMED: { text: 'Đã mua', cls: 'bg-green-50 text-green-700 border-green-200' },
                    CHECKED_IN: { text: 'Đã vào', cls: 'bg-blue-50 text-blue-700 border-blue-200' },
                    CANCELLED: { text: 'Đã hủy', cls: 'bg-gray-50 text-gray-700 border-gray-200' },
                  };
                  const currentStatus = statusMap[b.status] || { text: b.status, cls: 'bg-gray-50 text-gray-700 border-gray-200' };

                  return (
                    <tr key={b.bookingCode} className="hover:bg-[#FAFAFA]/50 transition-colors">
                      <td className="p-4 pl-6 font-bold text-[#22232B] font-mono tracking-wider">
                        {b.bookingCode}
                      </td>
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <User className="w-3.5 h-3.5 text-[#6B7280]" />
                          <div>
                            <div className="font-bold text-[#22232B]">
                              {b.customerName || 'Khách vãng lai'}
                            </div>
                            {b.customerPhone && (
                              <div className="text-[11px] text-[#6B7280] font-semibold">
                                {b.customerPhone}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="space-y-0.5">
                          <div className="font-bold text-[#22232B] line-clamp-1">{b.movieTitle}</div>
                          <div className="text-[11px] text-[#6B7280] font-semibold flex items-center gap-1">
                            <Calendar className="w-3 h-3" />
                            {new Date(b.showTime).toLocaleString('vi-VN')} | Phòng: {b.roomName}
                          </div>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="space-y-1">
                          <div className="text-xs font-bold text-indigo-600">
                            Ghế: {(b.seatCodes || []).join(', ')}
                          </div>
                          {(b.comboSummary || []).length > 0 && (
                            <div className="text-[11px] text-[#6B7280] flex items-center gap-1 font-medium bg-[#FAFAFA] border border-black/5 p-1 px-2 rounded-lg max-w-max">
                              <ShoppingBag className="w-3 h-3 text-[#F5A623]" />
                              {(b.comboSummary || []).join(', ')}
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="p-4 font-bold text-[#F5A623]">
                        {new Intl.NumberFormat('vi-VN').format(b.totalPrice)}đ
                      </td>
                      <td className="p-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-lg text-[10px] font-bold uppercase tracking-wider border ${currentStatus.cls}`}>
                          {currentStatus.text}
                        </span>
                      </td>
                      <td className="p-4 pr-6 text-right">
                        {b.status === 'CONFIRMED' ? (
                          <button
                            onClick={() => handleCheckIn(b.bookingCode)}
                            className="px-3.5 py-1.5 bg-gradient-to-r from-[#E5133A] to-[#8B0020] hover:opacity-95 text-white text-xs font-bold rounded-xl active:scale-[0.98] transition-all shadow-md shadow-[#E5133A]/10"
                          >
                            Soát Vé
                          </button>
                        ) : b.status === 'CHECKED_IN' ? (
                          <span className="inline-flex items-center gap-1 text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-xl">
                            <Check className="w-3.5 h-3.5" /> Vào rạp
                          </span>
                        ) : (
                          <span className="text-xs text-[#6B7280]/60 font-semibold">—</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
