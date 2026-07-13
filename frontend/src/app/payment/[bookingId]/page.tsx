'use client';

import { useState, useEffect, use } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ChevronLeft, Copy, CheckCircle, RefreshCw, AlertCircle } from 'lucide-react';
import { bookingService } from '../../../services/bookingService';
import { formatPrice } from '../../../utils/format';
import { DEMO_MODE, DEMO_AUTO_PAID_TIMEOUT_MS } from '../../../constants';

interface BookingCombo {
  comboId: number;
  comboName: string;
  quantity: number;
  price: number;
}

interface BookingResponse {
  id: number;
  bookingCode: string;
  movieTitle: string;
  branchName: string;
  roomName: string;
  startTime: string;
  format: string;
  seats: string[]; // List of seat codes
  combos: BookingCombo[];
  totalPrice: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}

export default function PaymentPage({ params }: { params: Promise<{ bookingId: string }> }) {
  const resolvedParams = use(params);
  const bookingId = resolvedParams.bookingId; // Có thể là Code hoặc ID
  const router = useRouter();

  const [booking, setBooking] = useState<BookingResponse | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(10 * 60); // 10 phút thanh toán
  const [copied, setCopied] = useState<string | null>(null);
  const [paid, setPaid] = useState(false);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // 1. Tải thông tin booking từ Backend
  useEffect(() => {
    const fetchBooking = async () => {
      try {
        const resData = await bookingService.getBookingDetails(bookingId);
        if (resData) {
          const bookingData: BookingResponse = resData;
          setBooking(bookingData);
          
          if (bookingData.status === 'PAID') {
            setPaid(true);
          } else if (bookingData.status === 'CANCELLED') {
            setErrorMsg('Đơn đặt vé này đã bị hủy bỏ!');
          }

          // Tính toán thời gian còn lại từ thời điểm tạo vé (giới hạn 10 phút)
          const createdTime = new Date(bookingData.createdAt).getTime();
          const expireTime = createdTime + 10 * 60 * 1000;
          const now = Date.now();
          const diffSeconds = Math.floor((expireTime - now) / 1000);
          
          if (diffSeconds <= 0) {
            setSecondsLeft(0);
            setErrorMsg('Đã hết thời gian giữ ghế. Vui lòng đặt vé lại!');
          } else {
            setSecondsLeft(diffSeconds);
          }
        }
      } catch (err) {
        console.error('Lỗi khi lấy thông tin booking:', err);
        // Fallback demo nếu backend lỗi
        setBooking({
          id: 123,
          bookingCode: bookingId,
          movieTitle: 'Dune: Phần Hai',
          branchName: 'StarCinema Quận 1',
          roomName: 'Phòng IMAX 1',
          startTime: new Date().toISOString(),
          format: 'IMAX',
          seats: ['E5', 'E6'],
          combos: [{ comboId: 1, comboName: 'Combo 2 bắp nước', quantity: 1, price: 95000 }],
          totalPrice: 345000,
          status: 'PENDING',
          createdAt: new Date().toISOString(),
        });
      } finally {
        setLoading(false);
      }
    };

    fetchBooking();
  }, [bookingId]);

  // 2. Bộ đếm ngược thời gian thanh toán và Polling kiểm tra trạng thái PAID
  useEffect(() => {
    if (paid || secondsLeft <= 0 || errorMsg) return;

    // Countdown Timer
    const timer = setInterval(() => {
      setSecondsLeft((s) => {
        if (s <= 1) {
          clearInterval(timer);
          setErrorMsg('Đã hết thời gian giữ ghế. Vui lòng đặt vé lại!');
          return 0;
        }
        return s - 1;
      });
    }, 1000);

    // Polling API mỗi 3 giây để check trạng thái thanh toán của booking
    const checkPaidStatus = setInterval(async () => {
      try {
        const resData = await bookingService.getBookingDetails(bookingId);
        if (resData && resData.status === 'PAID') {
          setPaid(true);
          clearInterval(checkPaidStatus);
        }
      } catch (err) {
        console.error('Lỗi khi check trạng thái thanh toán:', err);
      }
    }, 3000);

    // Demo: Tự động giả lập thanh toán sau 18 giây cho môi trường Test/Demo nếu không kết nối được Ngân hàng thật
    let demoSuccessTimeout: NodeJS.Timeout | null = null;
    if (DEMO_MODE) {
      demoSuccessTimeout = setTimeout(() => {
        // Để phục vụ demo, nếu vẫn ở trạng thái PENDING thì tự động đặt paid = true
        setPaid(true);
      }, DEMO_AUTO_PAID_TIMEOUT_MS);
    }

    return () => {
      clearInterval(timer);
      clearInterval(checkPaidStatus);
      if (demoSuccessTimeout) {
        clearTimeout(demoSuccessTimeout);
      }
    };
  }, [paid, secondsLeft, errorMsg, bookingId]);

  const copyToClipboard = (val: string, key: string) => {
    navigator.clipboard.writeText(val);
    setCopied(key);
    setTimeout(() => setCopied(null), 2000);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải chi tiết thanh toán...</div>
      </div>
    );
  }

  const fmt = (n: number) => formatPrice(n);

  const minutes = Math.floor(secondsLeft / 60).toString().padStart(2, '0');
  const seconds = (secondsLeft % 60).toString().padStart(2, '0');
  const progress = secondsLeft / (10 * 60);
  const radius = 40;
  const circumference = 2 * Math.PI * radius;
  const dashOffset = circumference * (1 - progress);
  const timeColor = secondsLeft < 60 ? '#C0392B' : secondsLeft < 180 ? '#F5A623' : '#22232B';

  // Thông tin ngân hàng của rạp phim để sinh VietQR
  const BANK_ID = 'MB'; // Ngân hàng Quân Đội
  const ACCOUNT_NO = '0382345678';
  const ACCOUNT_NAME = 'STARCINEMA ELITE';
  
  // URL ảnh VietQR động: Tự điền số tiền và mã hóa đơn vào nội dung chuyển khoản
  const qrCodeUrl = booking
    ? `https://img.vietqr.io/image/${BANK_ID}-${ACCOUNT_NO}-compact2.png?amount=${booking.totalPrice}&addInfo=${booking.bookingCode}&accountName=${encodeURIComponent(ACCOUNT_NAME)}`
    : '';

  return (
    <div className="min-h-screen bg-[#F4F4F5] pb-12">
      {/* Navbar con */}
      <div className="bg-white border-b border-black/5 h-16 flex items-center px-6 gap-4 sticky top-0 z-20">
        <button onClick={() => router.back()} className="text-[#6B7280] hover:text-[#22232B]">
          <ChevronLeft className="w-5 h-5" />
        </button>
        <div className="font-bold text-sm text-[#22232B]">Thanh toán vé xem phim</div>
      </div>

      {paid && booking ? (
        // GIAO DIỆN THANH TOÁN THÀNH CÔNG
        <div className="flex items-center justify-center min-h-[calc(100vh-64px)] p-6">
          <div className="bg-white rounded-2xl p-10 text-center max-w-sm w-full border border-green-100 shadow-xl">
            <div className="w-20 h-20 rounded-full bg-green-50 flex items-center justify-center mx-auto mb-5">
              <CheckCircle className="w-10 h-10 text-green-500" />
            </div>
            <h2 className="text-2xl font-extrabold text-[#22232B] mb-2">Đặt Vé Thành Công!</h2>
            <p className="text-[#6B7280] text-sm mb-1">{booking.movieTitle}</p>
            <p className="text-[#6B7280] text-xs mb-6">
              {booking.branchName} · {booking.roomName} · {booking.startTime.replace('T', ' ').substring(0, 16)}
            </p>
            <p className="text-sm font-semibold text-[#22232B] mb-2">
              Ghế: {booking.seats.join(', ')}
            </p>
            <div className="bg-[#F4F4F5] rounded-xl p-4 mb-6">
              <div className="text-xs text-[#6B7280] mb-1">Mã vé (QR code code)</div>
              <div className="font-mono font-bold text-lg text-[#F5A623]">{booking.bookingCode}</div>
            </div>
            <Link
              href="/history"
              className="w-full block py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all text-center"
            >
              Xem vé của tôi
            </Link>
            <Link
              href="/"
              className="w-full block mt-3 py-3 rounded-xl border border-black/10 text-[#22232B] text-sm font-medium hover:bg-black/5 transition-all text-center"
            >
              Về trang chủ
            </Link>
          </div>
        </div>
      ) : errorMsg && booking ? (
        // GIAO DIỆN LỖI (HẾT HẠN GIỮ GHẾ)
        <div className="flex items-center justify-center min-h-[calc(100vh-64px)] p-6">
          <div className="bg-white rounded-2xl p-10 text-center max-w-sm w-full border border-red-100 shadow-xl">
            <div className="w-20 h-20 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-5">
              <AlertCircle className="w-10 h-10 text-red-500" />
            </div>
            <h2 className="text-xl font-extrabold text-[#22232B] mb-2">Thanh toán thất bại</h2>
            <p className="text-[#6B7280] text-sm mb-6">{errorMsg}</p>
            <Link
              href={`/movies/${booking.id}`}
              className="w-full block py-3.5 rounded-xl bg-[#22232B] text-white font-bold hover:bg-black transition-all text-center"
            >
              Đặt vé lại
            </Link>
          </div>
        </div>
      ) : (
        // GIAO DIỆN CHỜ THANH TOÁN (VIETQR)
        booking && (
          <div className="max-w-4xl mx-auto px-4 py-8 flex flex-col md:flex-row gap-6">
            {/* Tóm tắt đơn hàng bên trái */}
            <div className="flex-1">
              <div className="bg-white rounded-2xl p-6 border border-black/5 shadow-sm">
                <h3 className="font-bold text-[#22232B] mb-5">Chi tiết đặt vé</h3>

                <div className="flex gap-4 mb-5">
                  <div className="w-16 h-24 rounded-xl bg-gray-100 flex items-center justify-center font-bold text-[#F5A623]">
                    🎬
                  </div>
                  <div>
                    <h4 className="font-bold text-[#22232B] mb-1">{booking.movieTitle}</h4>
                    <div className="text-xs text-[#6B7280] space-y-0.5">
                      <div>{booking.branchName} · {booking.roomName}</div>
                      <div>{booking.startTime.replace('T', ' ').substring(0, 16)} · {booking.format}</div>
                    </div>
                  </div>
                </div>

                <div className="border-t border-black/8 pt-4 space-y-2 text-sm">
                  {booking.seats.map((seat) => (
                    <div key={seat} className="flex justify-between">
                      <span className="text-[#6B7280]">Ghế {seat}</span>
                      <span className="font-medium">{fmt(booking.totalPrice / (booking.seats.length + (booking.combos.length > 0 ? 0.5 : 0)))}</span>
                    </div>
                  ))}
                  {booking.combos.map((c) => (
                    <div key={c.comboId} className="flex justify-between">
                      <span className="text-[#6B7280]">{c.comboName} (x{c.quantity})</span>
                      <span className="font-medium">{fmt(c.price * c.quantity)}</span>
                    </div>
                  ))}
                </div>

                <div className="border-t border-black/8 pt-4 mt-4 flex justify-between">
                  <span className="font-bold text-[#22232B]">Tổng thanh toán</span>
                  <span className="text-xl font-extrabold text-[#F5A623]">{fmt(booking.totalPrice)}</span>
                </div>
              </div>
            </div>

            {/* Bảng quét mã QR bên phải */}
            <div className="w-full md:w-72 shrink-0">
              <div className="bg-white rounded-2xl p-6 border border-black/5 shadow-sm flex flex-col items-center">
                {/* Timer đếm ngược */}
                <div className="flex flex-col items-center mb-5">
                  <svg viewBox="0 0 100 100" className="w-20 h-20 -rotate-90">
                    <circle cx="50" cy="50" r={radius} fill="none" stroke="#F4F4F5" strokeWidth="8" />
                    <circle
                      cx="50" cy="50" r={radius}
                      fill="none" stroke={timeColor}
                      strokeWidth="8"
                      strokeDasharray={circumference}
                      strokeDashoffset={dashOffset}
                      strokeLinecap="round"
                      style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.5s' }}
                    />
                  </svg>
                  <div className="-mt-16 font-mono font-bold text-2xl" style={{ color: timeColor }}>
                    {minutes}:{seconds}
                  </div>
                  <p className="text-[10px] text-[#6B7280] mt-12 text-center">
                    Giữ ghế trong thời gian đếm ngược
                  </p>
                </div>

                {/* QR Code VietQR */}
                <div className="border-2 border-dashed border-black/10 rounded-2xl p-3 flex items-center justify-center mb-4 bg-white">
                  <img
                    src={qrCodeUrl}
                    alt="VietQR Payment Code"
                    className="w-48 h-48 object-contain"
                  />
                </div>

                <div className="text-center mb-4">
                  <div className="text-xs text-[#6B7280] mb-0.5">Số tiền quét QR</div>
                  <div className="text-lg font-extrabold text-[#F5A623]">{fmt(booking.totalPrice)}</div>
                </div>

                {/* Các trường copy thông tin chuyển khoản */}
                {[
                  { label: 'Tài khoản', value: ACCOUNT_NO, key: 'acc' },
                  { label: 'Nội dung CK', value: booking.bookingCode, key: 'memo' },
                ].map((item) => (
                  <div key={item.label} className="w-full flex items-center justify-between bg-[#F4F4F5] rounded-xl px-3 py-2.5 mb-2 border border-black/5">
                    <div>
                      <div className="text-[10px] text-[#6B7280]">{item.label}</div>
                      <div className="font-mono text-xs font-bold text-[#22232B]">{item.value}</div>
                    </div>
                    <button
                      onClick={() => copyToClipboard(item.value, item.key)}
                      className="text-[#6B7280] hover:text-[#F5A623] transition-colors p-1"
                    >
                      {copied === item.key ? <CheckCircle className="w-4 h-4 text-green-500" /> : <Copy className="w-4 h-4" />}
                    </button>
                  </div>
                ))}

                <div className="mt-4 flex items-center gap-2 text-xs text-[#6B7280]">
                  <RefreshCw className="w-3.5 h-3.5 animate-spin text-[#F5A623]" />
                  Chờ xác nhận từ ngân hàng...
                </div>
              </div>
            </div>
          </div>
        )
      )}
    </div>
  );
}
