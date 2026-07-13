'use client';

import { useState, useEffect, use } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ChevronLeft, Minus, Plus, Tag, X } from 'lucide-react';
import { movieService } from '../../../services/movieService';
import { bookingService } from '../../../services/bookingService';
import { formatPrice } from '../../../utils/format';
import { useAuthStore } from '../../../store/authStore';

import { getWsUrl } from '../../../constants';

interface Showtime {
  id: number;
  movieTitle: string;
  roomName: string;
  branchName: string;
  startTime: string;
  format: string;
}

interface Seat {
  id: number;
  rowChar: string;
  colNum: number;
  seatCode: string;
  seatType: string;
  price: number;
  available: boolean;
  // Trạng thái cục bộ:
  isLockedByOther?: boolean;
  isLockedByMe?: boolean;
}

interface Combo {
  id: number;
  name: string;
  price: number;
  description?: string;
  imageUrl?: string;
}

const fallbackCombos: Combo[] = [
  { id: 1, name: 'Combo 1: Bắp ngọt + 1 Nước ngọt', price: 65000, description: '1 bắp ngọt lớn + 1 nước uống mát lạnh', imageUrl: 'https://images.unsplash.com/photo-1585647347483-22b66260dfff?w=120&h=80&fit=crop&auto=format' },
  { id: 2, name: 'Combo 2: Bắp ngọt + 2 Nước ngọt', price: 95000, description: '1 bắp ngọt lớn + 2 nước uống mát lạnh', imageUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=120&h=80&fit=crop&auto=format' },
  { id: 3, name: 'Combo 3: Bắp lớn + Nachos + 2 Nước', price: 145000, description: '1 bắp lớn + 1 Nachos + 2 nước uống', imageUrl: 'https://images.unsplash.com/photo-1574162565035-00c68dbb75e5?w=120&h=80&fit=crop&auto=format' },
];

export default function BookingPage({ params }: { params: Promise<{ showtimeId: string }> }) {
  const resolvedParams = use(params);
  const showtimeId = Number(resolvedParams.showtimeId);
  const router = useRouter();

  const { isAuthenticated } = useAuthStore();

  const [showtime, setShowtime] = useState<Showtime | null>(null);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [combos, setCombos] = useState<Combo[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const [comboQuantities, setComboQuantities] = useState<Record<number, number>>({});
  
  const [promoCode, setPromoCode] = useState('');
  const [promoApplied, setPromoApplied] = useState(false);
  const [discountPercent, setDiscountPercent] = useState(0);
  const [sessionId, setSessionId] = useState('');
  const [loading, setLoading] = useState(true);

  // Tạo Session ID ngẫu nhiên cho tab hiện tại
  useEffect(() => {
    if (typeof window !== 'undefined') {
      let sId = sessionStorage.getItem('booking_session_id');
      if (!sId) {
        sId = 'session_' + Math.random().toString(36).substring(2, 15);
        sessionStorage.setItem('booking_session_id', sId);
      }
      setSessionId(sId);
    }
  }, []);

  // Tải thông tin suất chiếu, sơ đồ ghế, và combo
  useEffect(() => {
    if (!sessionId) return;

    const fetchBookingData = async () => {
      setLoading(true);
      try {
        // 1. Lấy thông tin suất chiếu
        const showtimeRes = await movieService.getShowtimeDetails(showtimeId);
        if (showtimeRes?.data) {
          setShowtime(showtimeRes.data);
        }

        // 2. Lấy danh sách ghế thực tế
        const seatsRes = await movieService.getShowtimeSeats(showtimeId);
        let rawSeats: Seat[] = [];
        if (seatsRes?.data) {
          rawSeats = seatsRes.data;
        }

        // 3. Lấy danh sách ghế đang khóa của chính mình
        const myLockedRes = await bookingService.getMyLockedSeats(showtimeId, sessionId);
        const myLockedIds: number[] = myLockedRes?.data || [];

        // Đồng bộ trạng thái ghế
        const processedSeats = rawSeats.map((seat) => {
          const isByMe = myLockedIds.includes(seat.id);
          return {
            ...seat,
            // Nếu ghế không khả dụng và không phải do mình giữ, coi như bị người khác khóa
            isLockedByOther: !seat.available && !isByMe,
            isLockedByMe: isByMe,
          };
        });
        setSeats(processedSeats);
        setSelectedSeatIds(myLockedIds);

        // 4. Lấy danh sách combos từ backend
        const combosRes = await bookingService.getCombos().catch(() => null);
        if (combosRes?.data && combosRes.data.length > 0) {
          setCombos(combosRes.data);
        } else {
          setCombos(fallbackCombos);
        }
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu đặt vé:', err);
        // Fallback demo nếu lỗi kết nối
        setShowtime({
          id: showtimeId,
          movieTitle: 'Dune: Phần Hai',
          roomName: 'Phòng IMAX 1',
          branchName: 'StarCinema Quận 1',
          startTime: new Date().toISOString(),
          format: 'IMAX',
        });
        setCombos(fallbackCombos);
      } finally {
        setLoading(false);
      }
    };

    fetchBookingData();
  }, [showtimeId, sessionId]);

  // Thiết lập kết nối WebSocket đồng bộ ghế real-time
  useEffect(() => {
    if (!showtimeId || !sessionId) return;

    let socket: WebSocket;
    let connectTimeout: NodeJS.Timeout;

    const connectWebSocket = () => {
      // Kết nối trực tiếp qua endpoint WebSocket của Spring Boot
      const wsUrl = getWsUrl();
      socket = new WebSocket(wsUrl);

      socket.onopen = () => {
        console.log('Đã kết nối thành công WebSocket sync ghế.');
        
        // Gửi CONNECT frame STOMP
        const connectFrame = 'CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000';
        socket.send(connectFrame);

        // Đợi 200ms để server chấp nhận kết nối rồi SUBSCRIBE
        connectTimeout = setTimeout(() => {
          const subscribeFrame = `SUBSCRIBE\nid:sub-0\ndestination:/topic/showtime/${showtimeId}/seats\n\n\u0000`;
          socket.send(subscribeFrame);
        }, 200);
      };

      socket.onmessage = (event) => {
        const dataStr = event.data;
        // Phân tích message STOMP để lấy phần JSON payload
        if (dataStr.includes('MESSAGE')) {
          const bodyIndex = dataStr.indexOf('\n\n');
          if (bodyIndex !== -1) {
            const bodyStr = dataStr.substring(bodyIndex + 2, dataStr.lastIndexOf('\u0000')).trim();
            try {
              const payload = JSON.parse(bodyStr);
              const { seatId, action, sessionId: senderSessionId } = payload;
              
              // Nếu là thông báo từ tab/session khác
              if (senderSessionId !== sessionId) {
                setSeats((prevSeats) =>
                  prevSeats.map((seat) => {
                    if (seat.id === seatId) {
                      if (action === 'HOLD') {
                        return { ...seat, isLockedByOther: true, available: false };
                      } else if (action === 'RELEASE') {
                        return { ...seat, isLockedByOther: false, available: true };
                      } else if (action === 'BOOKED') {
                        return { ...seat, isLockedByOther: true, available: false };
                      }
                    }
                    return seat;
                  })
                );
              }
            } catch (err) {
              console.error('Lỗi khi parse tin nhắn websocket:', err);
            }
          }
        }
      };

      socket.onclose = () => {
        console.log('Mất kết nối WebSocket. Thử lại sau 5 giây...');
        setTimeout(connectWebSocket, 5000);
      };
    };

    connectWebSocket();

    return () => {
      clearTimeout(connectTimeout);
      if (socket) {
        socket.close();
      }
    };
  }, [showtimeId, sessionId]);

  const toggleSeat = async (seat: Seat) => {
    if (seat.isLockedByOther) return;

    const isCurrentlySelected = selectedSeatIds.includes(seat.id);
    
    // Optimistic UI update
    setSelectedSeatIds((prev) =>
      isCurrentlySelected ? prev.filter((id) => id !== seat.id) : [...prev, seat.id]
    );

    setSeats((prevSeats) =>
      prevSeats.map((s) => {
        if (s.id === seat.id) {
          return {
            ...s,
            isLockedByMe: !isCurrentlySelected,
            available: isCurrentlySelected,
          };
        }
        return s;
      })
    );

    try {
      if (isCurrentlySelected) {
        // Nhả ghế
        await bookingService.releaseSeat(showtimeId, seat.id, sessionId);
      } else {
        // Giữ ghế
        await bookingService.holdSeat(showtimeId, seat.id, sessionId);
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi giữ ghế. Vui lòng chọn ghế khác!');
      
      // Revert lại trạng thái
      setSelectedSeatIds((prev) =>
        isCurrentlySelected ? [...prev, seat.id] : prev.filter((id) => id !== seat.id)
      );
      setSeats((prevSeats) =>
        prevSeats.map((s) => {
          if (s.id === seat.id) {
            return {
              ...s,
              isLockedByMe: isCurrentlySelected,
              available: !isCurrentlySelected,
            };
          }
          return s;
        })
      );
    }
  };

  const updateCombo = (id: number, delta: number) => {
    setComboQuantities((prev) => ({
      ...prev,
      [id]: Math.max(0, (prev[id] ?? 0) + delta),
    }));
  };

  const applyPromo = async () => {
    if (!promoCode) return;
    try {
      // API check mã khuyến mãi
      const res = await bookingService.checkPromotion(promoCode);
      if (res?.success && res?.data) {
        setPromoApplied(true);
        setDiscountPercent(res.data.discountPercentage || 10);
        alert('Áp dụng mã giảm giá thành công!');
      } else {
        alert('Mã giảm giá không hợp lệ hoặc đã hết hạn!');
      }
    } catch (err) {
      // Giảm giá demo 10% nếu gõ STAR10
      if (promoCode.toUpperCase() === 'STAR10') {
        setPromoApplied(true);
        setDiscountPercent(10);
        alert('Áp dụng mã giảm giá thử nghiệm thành công (10%)!');
      } else {
        alert('Mã giảm giá không hợp lệ!');
      }
    }
  };

  const handleCheckout = async () => {
    if (!isAuthenticated) {
      router.push(`/auth?message=${encodeURIComponent('Vui lòng đăng nhập trước khi thanh toán!')}`);
      return;
    }

    if (selectedSeatIds.length === 0) {
      alert('Vui lòng chọn ít nhất một ghế!');
      return;
    }

    try {
      // Chuẩn bị combo payload
      const selectedCombos = Object.entries(comboQuantities)
        .filter(([, qty]) => qty > 0)
        .map(([id, qty]) => ({
          comboId: Number(id),
          quantity: qty,
        }));

      // Gọi API tạo Booking mới
      // Spring Boot BookingController nhận BookingRequest
      const responseData = await bookingService.createBooking({
        showtimeId: showtimeId,
        seatIds: selectedSeatIds,
        combos: selectedCombos,
        promoCode: promoApplied ? promoCode : null,
        paymentMethod: 'VNPAY', // Đặt trực tuyến, mặc định thanh toán qua cổng online
        sessionId: sessionId,
      });

      if (responseData) {
        // Chuyển hướng sang trang thanh toán
        router.push(`/payment/${responseData.bookingCode || responseData.id}`);
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi tạo đơn đặt vé. Vui lòng kiểm tra lại!');
    }
  };

  const getSeatStyle = (seat: Seat) => {
    if (seat.isLockedByMe) return 'bg-[#F5A623] border-[#F5A623] text-white';
    if (seat.isLockedByOther) return 'bg-[#F4F4F5] border-transparent text-[#C4C4C4] cursor-not-allowed';
    if (seat.seatType === 'SWEETBOX') return 'bg-white border-[#F5A623]/50 text-[#C47D0A] hover:border-[#F5A623]';
    if (seat.seatType === 'VIP') return 'bg-white border-[#E09415]/40 text-[#C47D0A] hover:border-[#F5A623]';
    return 'bg-white border-black/15 text-[#22232B] hover:border-[#F5A623]';
  };

  // Tính tiền
  const seatTotal = selectedSeatIds.reduce((acc, id) => {
    const seat = seats.find((s) => s.id === id);
    return acc + (seat ? Number(seat.price) : 85000);
  }, 0);

  const comboTotal = combos.reduce((acc, c) => acc + c.price * (comboQuantities[c.id] ?? 0), 0);
  const discount = promoApplied ? Math.round((seatTotal + comboTotal) * (discountPercent / 100)) : 0;
  const total = seatTotal + comboTotal - discount;

  const fmt = (n: number) => formatPrice(n);

  // Nhóm ghế theo hàng để render
  const rowsMap = new Map<string, Seat[]>();
  seats.forEach((seat) => {
    const r = seat.rowChar;
    if (!rowsMap.has(r)) {
      rowsMap.set(r, []);
    }
    rowsMap.get(r)!.push(seat);
  });
  
  const sortedRows = Array.from(rowsMap.keys()).sort();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải sơ đồ phòng chiếu...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F4F4F5] pb-12">
      {/* Header */}
      <div className="bg-white border-b border-black/5 h-16 flex items-center px-6 gap-4 sticky top-0 z-20">
        <button onClick={() => router.back()} className="text-[#6B7280] hover:text-[#22232B]">
          <ChevronLeft className="w-5 h-5" />
        </button>
        {showtime && (
          <div>
            <div className="font-bold text-sm text-[#22232B]">{showtime.movieTitle}</div>
            <div className="text-xs text-[#6B7280]">
              {showtime.branchName} · {showtime.roomName} · {showtime.format} · {showtime.startTime.replace('T', ' ').substring(0, 16)}
            </div>
          </div>
        )}
      </div>

      <div className="max-w-6xl mx-auto px-4 py-6 flex flex-col lg:flex-row gap-6">
        {/* Left Seat Map & Combos */}
        <div className="flex-1 space-y-5">
          {/* Seat Grid */}
          <div className="bg-white rounded-2xl p-6 border border-black/5">
            <div className="flex flex-col items-center mb-6">
              <div className="w-2/3 h-1.5 bg-gradient-to-r from-transparent via-black/20 to-transparent rounded-full mb-1" />
              <span className="text-xs text-[#6B7280] font-medium tracking-widest uppercase">Màn hình</span>
            </div>

            {/* Render Grid */}
            <div className="overflow-x-auto">
              <div className="min-w-fit mx-auto flex flex-col gap-1.5 items-center">
                {sortedRows.map((row) => (
                  <div key={row} className="flex items-center gap-1.5">
                    <span className="text-xs text-[#6B7280] w-5 text-right shrink-0">{row}</span>
                    <div className="flex gap-1">
                      {rowsMap.get(row)!.sort((a, b) => a.colNum - b.colNum).map((seat) => (
                        <button
                          key={seat.id}
                          disabled={seat.isLockedByOther}
                          onClick={() => toggleSeat(seat)}
                          title={`${seat.seatCode} (${seat.seatType}) - ${fmt(Number(seat.price))}`}
                          className={`w-8 h-8 rounded-lg border text-xs font-semibold transition-all ${getSeatStyle(seat)}`}
                        >
                          {seat.seatType === 'SWEETBOX' && !seat.isLockedByMe ? '♥' : seat.colNum}
                        </button>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Legend */}
            <div className="flex flex-wrap justify-center gap-4 mt-8 pt-6 border-t border-black/5">
              {[
                { label: 'Standard', color: 'bg-white border border-black/15' },
                { label: 'VIP', color: 'bg-white border border-[#E09415]/40' },
                { label: 'Sweetbox (Đôi) ♥', color: 'bg-white border border-[#F5A623]/50' },
                { label: 'Bạn chọn', color: 'bg-[#F5A623]' },
                { label: 'Đang giữ/Đã bán', color: 'bg-[#F4F4F5]' },
              ].map((item) => (
                <div key={item.label} className="flex items-center gap-1.5 text-xs text-[#6B7280]">
                  <div className={`w-4 h-4 rounded-md ${item.color}`} />
                  {item.label}
                </div>
              ))}
            </div>
          </div>

          {/* Popcorn Combos */}
          <div className="bg-white rounded-2xl p-6 border border-black/5">
            <h3 className="font-bold text-[#22232B] mb-4">Thêm bắp nước ưu đãi</h3>
            <div className="flex gap-4 overflow-x-auto pb-1">
              {combos.map((combo) => (
                <div key={combo.id} className="shrink-0 w-52 border border-black/8 rounded-xl overflow-hidden bg-white">
                  {combo.imageUrl && (
                    <img src={combo.imageUrl} alt={combo.name} className="w-full h-24 object-cover" />
                  )}
                  <div className="p-3">
                    <div className="text-xs font-semibold text-[#22232B] leading-snug mb-1 truncate" title={combo.name}>
                      {combo.name}
                    </div>
                    <div className="text-sm font-bold text-[#F5A623] mb-2">{fmt(combo.price)}</div>
                    <div className="flex items-center justify-between">
                      <button
                        onClick={() => updateCombo(combo.id, -1)}
                        disabled={!comboQuantities[combo.id]}
                        className="w-7 h-7 rounded-lg border border-black/10 flex items-center justify-center hover:bg-black/5 disabled:opacity-35 transition-all"
                      >
                        <Minus className="w-3 h-3" />
                      </button>
                      <span className="font-bold text-sm text-[#22232B] w-6 text-center">
                        {comboQuantities[combo.id] ?? 0}
                      </span>
                      <button
                        onClick={() => updateCombo(combo.id, 1)}
                        className="w-7 h-7 rounded-lg bg-[#F5A623] flex items-center justify-center hover:bg-[#E09415] transition-all"
                      >
                        <Plus className="w-3 h-3 text-white" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Summary Panel */}
        <div className="w-full lg:w-80 shrink-0">
          <div className="bg-white rounded-2xl p-5 border border-black/5 sticky top-24 shadow-sm">
            <h3 className="font-bold text-[#22232B] mb-5">Chi tiết đặt vé</h3>

            {/* Selected Seats */}
            <div className="mb-4">
              <div className="text-xs font-semibold text-[#6B7280] uppercase tracking-wider mb-2">Ghế chọn</div>
              {selectedSeatIds.length === 0 ? (
                <div className="text-sm text-[#6B7280]">Chưa chọn ghế nào</div>
              ) : (
                <div className="flex flex-wrap gap-1.5">
                  {selectedSeatIds.map((id) => {
                    const seat = seats.find((s) => s.id === id);
                    if (!seat) return null;
                    return (
                      <span key={id} className="flex items-center gap-1 bg-[#FEF3DC] text-[#C47D0A] text-xs font-semibold px-2.5 py-1 rounded-lg">
                        {seat.seatCode}
                        <button onClick={() => toggleSeat(seat)} className="hover:text-red-500 ml-1">
                          <X className="w-3 h-3" />
                        </button>
                      </span>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Selected Combos */}
            {Object.entries(comboQuantities).filter(([, q]) => q > 0).length > 0 && (
              <div className="mb-4 pt-3 border-t border-black/5">
                <div className="text-xs font-semibold text-[#6B7280] uppercase tracking-wider mb-2">Combo bắp nước</div>
                {Object.entries(comboQuantities).filter(([, q]) => q > 0).map(([id, qty]) => {
                  const combo = combos.find((c) => c.id === Number(id));
                  if (!combo) return null;
                  return (
                    <div key={id} className="flex justify-between text-xs text-[#22232B] mb-1">
                      <span className="text-[#6B7280] truncate max-w-[150px]">{combo.name.split(':')[0]} ×{qty}</span>
                      <span className="font-semibold">{fmt(combo.price * qty)}</span>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Total calculation */}
            <div className="border-t border-black/8 pt-4 mb-4 space-y-2 text-sm">
              <div className="flex justify-between text-[#6B7280]">
                <span>Ghế ({selectedSeatIds.length})</span>
                <span>{fmt(seatTotal)}</span>
              </div>
              {comboTotal > 0 && (
                <div className="flex justify-between text-[#6B7280]">
                  <span>Combo</span>
                  <span>{fmt(comboTotal)}</span>
                </div>
              )}
              {promoApplied && (
                <div className="flex justify-between text-green-600">
                  <span>Khuyến mãi ({discountPercent}%)</span>
                  <span>-{fmt(discount)}</span>
                </div>
              )}
            </div>

            {/* Promo Code Input */}
            <div className="flex gap-2 mb-5">
              <div className="flex-1 flex items-center gap-2 bg-[#F4F4F5] rounded-xl px-3 py-2 border border-transparent focus-within:border-[#F5A623]">
                <Tag className="w-4 h-4 text-[#6B7280] shrink-0" />
                <input
                  placeholder="Mã giảm giá"
                  value={promoCode}
                  onChange={(e) => setPromoCode(e.target.value)}
                  className="flex-1 bg-transparent text-sm outline-none text-[#22232B] placeholder:text-[#6B7280]"
                />
              </div>
              <button
                onClick={applyPromo}
                className="px-3 py-2 rounded-xl bg-[#22232B] text-white text-xs font-semibold hover:bg-black transition-colors"
              >
                Áp dụng
              </button>
            </div>

            {/* Total Payment */}
            <div className="flex justify-between items-center mb-6 pt-3 border-t border-black/5">
              <span className="font-bold text-[#22232B]">Thanh toán</span>
              <span className="text-xl font-extrabold text-[#F5A623]">{fmt(total)}</span>
            </div>

            <button
              onClick={handleCheckout}
              disabled={selectedSeatIds.length === 0}
              className="w-full py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all disabled:opacity-40 disabled:cursor-not-allowed shadow-md shadow-[#F5A623]/20 active:scale-[0.98]"
            >
              Tiếp tục thanh toán
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
