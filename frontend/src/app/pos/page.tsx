'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ChevronRight, Star, Phone, Printer, Banknote, Smartphone, Check, Minus, Plus, Film, AlertCircle, ShoppingBag, X } from 'lucide-react';
import { movieService } from '../../services/movieService';
import { bookingService } from '../../services/bookingService';
import { customerService } from '../../services/customerService';
import { formatPrice } from '../../utils/format';
import { useAuthStore } from '../../store/authStore';

interface Movie {
  id: number;
  title: string;
  genre: string;
  duration: number;
  posterUrl: string;
}

interface Showtime {
  id: number;
  startTime: string;
  format: string;
  roomName: string;
  branchName: string;
  price: number;
}

interface Seat {
  id: number;
  rowChar: string;
  colNum: number;
  seatCode: string;
  seatType: string;
  price: number;
  available: boolean;
  isLockedByOther?: boolean;
  isLockedByMe?: boolean;
}

interface Combo {
  id: number;
  name: string;
  price: number;
  description?: string;
}

interface Customer {
  fullName: string;
  membershipLevel: string;
  points: number;
  discountRate: number;
}

const steps = ['Chọn phim', 'Chọn suất', 'Chọn ghế', 'Bắp nước', 'Thanh toán'];

const fallbackCombos: Combo[] = [
  { id: 1, name: 'Combo 1: Bắp ngọt + 1 Nước', price: 65000, description: '1 bắp lớn + 1 nước ngọt' },
  { id: 2, name: 'Combo 2: Bắp ngọt + 2 Nước', price: 95000, description: '1 bắp lớn + 2 nước ngọt' },
  { id: 3, name: 'Combo 3: Bắp lớn + Nachos + 2 Nước', price: 145000, description: '1 bắp lớn + 1 Nachos + 2 nước' },
];

export default function POSPage() {
  const router = useRouter();
  const { user, isAuthenticated, loading: authLoading } = useAuthStore();

  const [step, setStep] = useState(0);
  
  // Dữ liệu từ API
  const [movies, setMovies] = useState<Movie[]>([]);
  const [showtimes, setShowtimes] = useState<Showtime[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [combos, setCombos] = useState<Combo[]>([]);

  // Lựa chọn của nhân viên
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null);
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const [comboQuantities, setComboQuantities] = useState<Record<number, number>>({});
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD'>('CASH');

  // Tra cứu hội viên
  const [customerPhone, setCustomerPhone] = useState('');
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [searchingCustomer, setSearchingCustomer] = useState(false);

  // Session ID cho lock ghế
  const [sessionId, setSessionId] = useState('');
  const [loading, setLoading] = useState(false);

  // Trạng thái in hóa đơn sau khi chốt thành công
  const [printBooking, setPrintBooking] = useState<any | null>(null);

  // 1. Phân quyền POS: Chỉ Staff, Manager, Admin được vào
  useEffect(() => {
    if (!authLoading) {
      if (!isAuthenticated) {
        router.push('/auth?message=' + encodeURIComponent('Vui lòng đăng nhập tài khoản nhân viên!'));
      } else if (user && !['STAFF', 'MANAGER', 'ADMIN'].includes(user.role)) {
        router.push('/auth?message=' + encodeURIComponent('Tài khoản của bạn không có quyền truy cập POS!'));
      }
    }
  }, [isAuthenticated, user, authLoading, router]);

  // 2. Khởi tạo Session ID
  useEffect(() => {
    if (typeof window !== 'undefined') {
      let sId = sessionStorage.getItem('pos_session_id');
      if (!sId) {
        sId = 'pos_session_' + Math.random().toString(36).substring(2, 15);
        sessionStorage.setItem('pos_session_id', sId);
      }
      setSessionId(sId);
    }
  }, []);

  // 3. Tải danh sách phim
  useEffect(() => {
    const fetchMovies = async () => {
      try {
        const resData = await movieService.getNowPlaying();
        if (resData?.data) {
          setMovies(resData.data);
        }
      } catch (err) {
        console.error('Lỗi khi tải danh sách phim POS:', err);
      }
    };
    fetchMovies();
  }, []);

  // 4. Khi chọn phim, tải suất chiếu của phim đó tại chi nhánh nhân viên (nếu có)
  useEffect(() => {
    if (!selectedMovie) return;

    const fetchShowtimes = async () => {
      try {
        // Tải lịch chiếu ngày hôm nay
        const today = new Date().toISOString().split('T')[0];
        const resData = await movieService.getShowtimes(selectedMovie.id, today);
        if (resData?.data) {
          setShowtimes(resData.data);
        } else {
          setShowtimes([]);
        }
      } catch (err) {
        console.error('Lỗi tải suất chiếu POS:', err);
        setShowtimes([]);
      }
    };
    fetchShowtimes();
    setSelectedShowtime(null);
    setSelectedSeatIds([]);
    setStep(1);
  }, [selectedMovie]);

  // 5. Khi chọn suất chiếu, tải sơ đồ ghế và combo
  useEffect(() => {
    if (!selectedShowtime) return;

    const fetchShowtimeDetails = async () => {
      try {
        const seatsRes = await movieService.getShowtimeSeats(selectedShowtime.id);
        if (seatsRes?.data) {
          const rawSeats: Seat[] = seatsRes.data;
          setSeats(rawSeats.map(s => ({
            ...s,
            isLockedByOther: !s.available
          })));
        }

        const combosRes = await bookingService.getCombos().catch(() => null);
        if (combosRes?.data) {
          setCombos(combosRes.data);
        } else {
          setCombos(fallbackCombos);
        }
      } catch (err) {
        console.error('Lỗi tải chi tiết suất chiếu POS:', err);
      }
    };
    fetchShowtimeDetails();
    setSelectedSeatIds([]);
  }, [selectedShowtime]);

  // 6. WebSocket kết nối sơ đồ ghế đồng bộ real-time cho POS
  useEffect(() => {
    if (!selectedShowtime || !sessionId) return;

    let socket: WebSocket;
    let connectTimeout: NodeJS.Timeout;

    const connectWebSocket = () => {
      const wsUrl = `ws://${window.location.hostname}:8082/ws-cinema/websocket`;
      socket = new WebSocket(wsUrl);

      socket.onopen = () => {
        const connectFrame = 'CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000';
        socket.send(connectFrame);

        connectTimeout = setTimeout(() => {
          const subscribeFrame = `SUBSCRIBE\nid:sub-0\ndestination:/topic/showtime/${selectedShowtime.id}/seats\n\n\u0000`;
          socket.send(subscribeFrame);
        }, 200);
      };

      socket.onmessage = (event) => {
        const dataStr = event.data;
        if (dataStr.includes('MESSAGE')) {
          const bodyIndex = dataStr.indexOf('\n\n');
          if (bodyIndex !== -1) {
            const bodyStr = dataStr.substring(bodyIndex + 2, dataStr.lastIndexOf('\u0000')).trim();
            try {
              const payload = JSON.parse(bodyStr);
              const { seatId, action, sessionId: senderSessionId } = payload;
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
              console.error(err);
            }
          }
        }
      };

      socket.onclose = () => {
        setTimeout(connectWebSocket, 5000);
      };
    };

    connectWebSocket();

    return () => {
      clearTimeout(connectTimeout);
      if (socket) socket.close();
    };
  }, [selectedShowtime, sessionId]);

  const toggleSeat = async (seat: Seat) => {
    if (seat.isLockedByOther) return;

    const isCurrentlySelected = selectedSeatIds.includes(seat.id);
    setSelectedSeatIds(prev =>
      isCurrentlySelected ? prev.filter(id => id !== seat.id) : [...prev, seat.id]
    );

    setSeats(prevSeats =>
      prevSeats.map(s => {
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
        await bookingService.releaseSeat(selectedShowtime!.id, seat.id, sessionId);
      } else {
        await bookingService.holdSeat(selectedShowtime!.id, seat.id, sessionId);
      }
    } catch (err) {
      console.error(err);
      // Revert UI
      setSelectedSeatIds(prev =>
        isCurrentlySelected ? [...prev, seat.id] : prev.filter(id => id !== seat.id)
      );
    }
  };

  const lookupCustomer = async () => {
    if (!customerPhone) return;
    setSearchingCustomer(true);
    setCustomer(null);
    try {
      const resData = await customerService.searchCustomerByPhone(customerPhone);
      if (resData?.success && resData?.data) {
        setCustomer(resData.data);
      } else {
        alert('Không tìm thấy hội viên với số điện thoại này.');
      }
    } catch (err) {
      console.error(err);
      alert('Không tìm thấy hội viên.');
    } finally {
      setSearchingCustomer(false);
    }
  };

  const updateCombo = (id: number, delta: number) => {
    setComboQuantities(prev => ({
      ...prev,
      [id]: Math.max(0, (prev[id] ?? 0) + delta),
    }));
  };

  const handleCreatePOSBooking = async () => {
    if (!selectedShowtime || selectedSeatIds.length === 0) return;
    setLoading(true);

    try {
      const selectedCombos = Object.entries(comboQuantities)
        .filter(([, qty]) => qty > 0)
        .map(([id, qty]) => ({
          comboId: Number(id),
          quantity: qty,
        }));

      // Gọi API chốt hóa đơn trực tiếp tại quầy của Staff
      const resData = await bookingService.createBooking({
        showtimeId: selectedShowtime.id,
        seatIds: selectedSeatIds,
        combos: selectedCombos,
        paymentMethod: paymentMethod, // CASH hoặc CARD
        sessionId: sessionId,
        customerPhone: customer ? customerPhone : null,
      } as any);

      if (resData) {
        setPrintBooking(resData);
        alert('Đặt vé tại quầy thành công!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Chốt vé thất bại. Vui lòng kiểm tra lại!');
    } finally {
      setLoading(false);
    }
  };

  const resetPOS = () => {
    setSelectedMovie(null);
    setSelectedShowtime(null);
    setSelectedSeatIds([]);
    setComboQuantities({});
    setCustomer(null);
    setCustomerPhone('');
    setPrintBooking(null);
    setStep(0);
  };

  // Tính tiền
  const seatTotal = selectedSeatIds.reduce((acc, id) => {
    const seat = seats.find(s => s.id === id);
    return acc + (seat ? Number(seat.price) : 85000);
  }, 0);

  const comboTotal = combos.reduce((acc, c) => acc + c.price * (comboQuantities[c.id] ?? 0), 0);
  const subTotal = seatTotal + comboTotal;
  const discount = customer ? Math.round(subTotal * (customer.discountRate || 0)) : 0;
  const total = subTotal - discount;

  const fmt = (n: number) => formatPrice(n);

  // Sắp xếp ghế theo hàng
  const rowsMap = new Map<string, Seat[]>();
  seats.forEach(s => {
    const r = s.rowChar;
    if (!rowsMap.has(r)) rowsMap.set(r, []);
    rowsMap.get(r)!.push(s);
  });
  const sortedRows = Array.from(rowsMap.keys()).sort();

  return (
    <div className="h-screen bg-[#F4F4F5] flex flex-col overflow-hidden pt-16">
      {/* POS Title Header */}
      <div className="bg-white border-b border-black/5 h-16 flex items-center px-6 justify-between shrink-0">
        <div className="flex items-center gap-2">
          <Film className="w-5 h-5 text-[#F5A623]" />
          <span className="font-extrabold text-base text-[#22232B]">Quầy bán vé StarCinema Elite</span>
        </div>
        <button onClick={() => router.push('/')} className="text-sm font-semibold text-red-500 hover:text-red-700">
          Thoát POS
        </button>
      </div>

      {/* Steps Indicator */}
      <div className="bg-white border-b border-black/5 px-6 py-3 flex items-center gap-2 shrink-0 overflow-x-auto">
        {steps.map((s, i) => (
          <div key={s} className="flex items-center gap-2">
            <button
              onClick={() => {
                if (i < step && selectedMovie) setStep(i);
              }}
              className={`flex items-center gap-2 text-xs font-semibold transition-all ${
                i === step ? 'text-[#22232B]' : i < step ? 'text-[#F5A623]' : 'text-[#C4C4C4]'
              }`}
            >
              <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold ${
                i < step ? 'bg-[#F5A623] text-white' :
                i === step ? 'bg-[#22232B] text-white' :
                'bg-[#F4F4F5] text-[#C4C4C4]'
              }`}>
                {i < step ? <Check className="w-3.5 h-3.5" /> : i + 1}
              </div>
              <span>{s}</span>
            </button>
            {i < steps.length - 1 && <ChevronRight className="w-4 h-4 text-[#C4C4C4] shrink-0" />}
          </div>
        ))}
      </div>

      {/* Main Action Workspaces */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left: Step specific layout */}
        <div className="flex-1 overflow-y-auto p-5">
          {step === 0 && (
            <div>
              <h2 className="text-base font-bold text-[#22232B] mb-4">Danh sách phim đang chiếu</h2>
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                {movies.map((movie) => (
                  <button
                    key={movie.id}
                    onClick={() => setSelectedMovie(movie)}
                    className="rounded-2xl overflow-hidden border border-black/5 transition-all text-left bg-white hover:border-[#F5A623] p-2 flex flex-col items-center"
                  >
                    <img src={movie.posterUrl} alt={movie.title} className="w-full h-40 object-cover rounded-xl bg-gray-50" />
                    <div className="p-2 w-full text-center">
                      <div className="font-bold text-xs text-[#22232B] truncate">{movie.title}</div>
                      <div className="text-[10px] text-[#6B7280] mt-0.5">{movie.duration} phút · {movie.genre}</div>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 1 && selectedMovie && (
            <div>
              <h2 className="text-base font-bold text-[#22232B] mb-4">Lịch chiếu hôm nay — {selectedMovie.title}</h2>
              {showtimes.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {showtimes.map((st) => (
                    <button
                      key={st.id}
                      onClick={() => {
                        setSelectedShowtime(st);
                        setStep(2);
                      }}
                      className="p-4 rounded-xl border border-black/10 bg-white hover:border-[#F5A623] hover:shadow transition-all text-center"
                    >
                      <div className="text-lg font-bold text-[#22232B]">{st.startTime.split('T')[1].substring(0, 5)}</div>
                      <div className="text-xs text-[#6B7280] mt-1">{st.roomName} · {st.format}</div>
                    </button>
                  ))}
                </div>
              ) : (
                <div className="text-center p-8 bg-white border border-black/5 rounded-2xl text-xs text-[#6B7280]">
                  Không có suất chiếu nào hôm nay cho phim này.
                </div>
              )}
            </div>
          )}

          {step === 2 && selectedShowtime && (
            <div>
              <h2 className="text-base font-bold text-[#22232B] mb-4">Sơ đồ phòng chiếu ({selectedShowtime.roomName})</h2>
              <div className="bg-white rounded-2xl p-6 border border-black/5 flex flex-col items-center shadow-sm">
                <div className="w-2/3 h-1 bg-black/10 rounded-full mb-6 text-center text-[10px] text-[#6B7280] tracking-widest uppercase">
                  Màn hình
                </div>

                <div className="overflow-x-auto w-full flex justify-center">
                  <div className="min-w-fit flex flex-col gap-1.5">
                    {sortedRows.map((row) => (
                      <div key={row} className="flex items-center gap-1.5">
                        <span className="text-[10px] text-[#6B7280] w-4 text-right">{row}</span>
                        <div className="flex gap-1">
                          {rowsMap.get(row)!.sort((a, b) => a.colNum - b.colNum).map((seat) => (
                            <button
                              key={seat.id}
                              disabled={seat.isLockedByOther}
                              onClick={() => toggleSeat(seat)}
                              className={`w-7 h-7 rounded-md border text-[10px] font-bold transition-all ${
                                seat.isLockedByMe ? 'bg-[#F5A623] border-[#F5A623] text-white' :
                                seat.isLockedByOther ? 'bg-[#F4F4F5] border-transparent text-[#C4C4C4] cursor-not-allowed' :
                                'bg-white border-black/15 hover:border-[#F5A623]'
                              }`}
                            >
                              {seat.colNum}
                            </button>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="flex gap-4 mt-6 text-[10px] text-[#6B7280]">
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-[#F5A623]" /> Đang chọn</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-[#F4F4F5]" /> Đã khóa/bán</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded border border-black/15 bg-white" /> Trống</span>
                </div>
              </div>
            </div>
          )}

          {step === 3 && (
            <div>
              <h2 className="text-base font-bold text-[#22232B] mb-4">Combo bắp nước</h2>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                {combos.map((combo) => (
                  <div key={combo.id} className="bg-white rounded-2xl p-4 border border-black/5 shadow-sm">
                    <div className="font-bold text-xs text-[#22232B] mb-1">{combo.name}</div>
                    <div className="text-[10px] text-[#6B7280] mb-3">{combo.description || 'Chi tiết combo'}</div>
                    <div className="text-sm font-extrabold text-[#F5A623] mb-4">{fmt(combo.price)}</div>
                    <div className="flex items-center gap-3">
                      <button onClick={() => updateCombo(combo.id, -1)} className="w-7 h-7 rounded-lg border border-black/10 flex items-center justify-center hover:bg-black/5 disabled:opacity-30" disabled={!comboQuantities[combo.id]}>
                        <Minus className="w-3 h-3" />
                      </button>
                      <span className="text-sm font-bold text-[#22232B] w-4 text-center">{comboQuantities[combo.id] ?? 0}</span>
                      <button onClick={() => updateCombo(combo.id, 1)} className="w-7 h-7 rounded-lg bg-[#F5A623] flex items-center justify-center hover:bg-[#E09415]">
                        <Plus className="w-3 h-3 text-white" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {step === 4 && (
            <div>
              <h2 className="text-base font-bold text-[#22232B] mb-4">Phương thức thanh toán & Chốt vé</h2>
              <div className="grid grid-cols-2 gap-4 max-w-sm">
                {[
                  { key: 'CASH' as const, label: 'Tiền mặt', icon: Banknote },
                  { key: 'CARD' as const, label: 'Quẹt thẻ POS', icon: Smartphone },
                ].map((m) => {
                  const Icon = m.icon;
                  return (
                    <button
                      key={m.key}
                      onClick={() => setPaymentMethod(m.key)}
                      className={`flex flex-col items-center gap-2 p-5 rounded-2xl border-2 font-bold text-sm transition-all ${
                        paymentMethod === m.key
                          ? 'border-[#F5A623] bg-[#FEF3DC] text-[#C47D0A]'
                          : 'border-black/10 bg-white text-[#22232B] hover:border-black/20'
                      }`}
                    >
                      <Icon className="w-6 h-6" />
                      {m.label}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Stepper Navigation Buttons */}
          <div className="mt-6 flex gap-3">
            {step > 0 && (
              <button
                onClick={() => setStep(step - 1)}
                className="px-5 py-2.5 rounded-xl border border-black/10 text-xs font-semibold text-[#22232B] hover:bg-black/5"
              >
                ← Quay lại
              </button>
            )}
            {step < 4 && (
              <button
                onClick={() => setStep(step + 1)}
                disabled={(step === 0 && !selectedMovie) || (step === 1 && !selectedShowtime) || (step === 2 && selectedSeatIds.length === 0)}
                className="px-6 py-2.5 rounded-xl bg-[#22232B] text-white text-xs font-bold hover:bg-black transition-all disabled:opacity-30 disabled:cursor-not-allowed"
              >
                Tiếp theo →
              </button>
            )}
          </div>
        </div>

        {/* Right sidebar: Invoice summary */}
        <div className="w-80 shrink-0 bg-white border-l border-black/5 flex flex-col overflow-y-auto">
          {/* Customer member info lookup */}
          <div className="p-4 border-b border-black/5">
            <div className="text-[10px] font-bold text-[#6B7280] uppercase tracking-wider mb-2">Hội viên tích điểm</div>
            <div className="flex gap-2">
              <div className="flex-1 flex items-center gap-2 bg-[#F4F4F5] rounded-xl px-3 py-1.5 border border-transparent focus-within:border-[#F5A623]">
                <Phone className="w-3.5 h-3.5 text-[#6B7280] shrink-0" />
                <input
                  placeholder="Số điện thoại"
                  value={customerPhone}
                  onChange={(e) => setCustomerPhone(e.target.value)}
                  className="flex-1 bg-transparent text-xs outline-none text-[#22232B] placeholder:text-[#6B7280]"
                />
              </div>
              <button
                onClick={lookupCustomer}
                disabled={searchingCustomer}
                className="px-3 py-1.5 rounded-xl bg-[#22232B] text-white text-xs font-bold hover:bg-black transition-colors"
              >
                Tìm
              </button>
            </div>
            {customer && (
              <div className="mt-2 flex items-center gap-2 bg-[#FEF3DC] rounded-xl p-2">
                <div className="w-6 h-6 rounded-full bg-[#F5A623] flex items-center justify-center text-[10px] font-bold text-white shrink-0">
                  {customer.fullName[0].toUpperCase()}
                </div>
                <div>
                  <div className="text-[10px] font-bold text-[#22232B]">{customer.fullName}</div>
                  <div className="flex items-center gap-1 text-[8px] text-[#C47D0A] font-semibold uppercase">
                    <Star className="w-2 h-2 fill-current" />
                    {customer.membershipLevel} (Giảm {(customer.discountRate * 100).toFixed(0)}%)
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Invoice ticket details */}
          <div className="flex-1 p-4">
            <div className="text-[10px] font-bold text-[#6B7280] uppercase tracking-wider mb-3">Tóm tắt đơn POS</div>
            {selectedMovie ? (
              <div className="space-y-3 text-xs">
                <div className="flex justify-between">
                  <span className="text-[#6B7280]">Phim</span>
                  <span className="font-semibold text-[#22232B] text-right truncate max-w-[150px]">{selectedMovie.title}</span>
                </div>
                {selectedShowtime && (
                  <>
                    <div className="flex justify-between">
                      <span className="text-[#6B7280]">Giờ chiếu</span>
                      <span className="font-semibold text-[#22232B]">
                        {selectedShowtime.startTime.split('T')[1].substring(0, 5)} · {selectedShowtime.roomName}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-[#6B7280]">Định dạng</span>
                      <span className="font-semibold text-[#22232B]">{selectedShowtime.format}</span>
                    </div>
                  </>
                )}
                {selectedSeatIds.length > 0 && (
                  <div className="flex justify-between">
                    <span className="text-[#6B7280]">Ghế ({selectedSeatIds.length})</span>
                    <span className="font-semibold text-[#22232B]">
                      {selectedSeatIds.map(id => seats.find(s => s.id === id)?.seatCode).join(', ')}
                    </span>
                  </div>
                )}
                {Object.values(comboQuantities).some(q => q > 0) && (
                  <div className="border-t border-black/5 pt-2 space-y-1">
                    <div className="text-[10px] font-bold text-[#6B7280]">Bắp nước</div>
                    {Object.entries(comboQuantities).map(([id, qty]) => {
                      if (qty === 0) return null;
                      const combo = combos.find(c => c.id === Number(id));
                      return (
                        <div key={id} className="flex justify-between text-[#6B7280]">
                          <span>{combo?.name.split(':')[0]} x{qty}</span>
                          <span>{fmt((combo?.price || 0) * qty)}</span>
                        </div>
                      );
                    })}
                  </div>
                )}

                <div className="border-t border-black/8 pt-3 space-y-1.5">
                  <div className="flex justify-between">
                    <span className="text-[#6B7280]">Tạm tính</span>
                    <span className="font-medium text-[#22232B]">{fmt(subTotal)}</span>
                  </div>
                  {discount > 0 && (
                    <div className="flex justify-between text-green-600">
                      <span>Giảm giá thành viên</span>
                      <span>-{fmt(discount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between font-extrabold text-sm border-t border-black/5 pt-2">
                    <span className="text-[#22232B]">Tổng thu</span>
                    <span className="text-[#F5A623]">{fmt(total)}</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-xs text-[#6B7280] italic">Hãy chọn phim để bắt đầu đơn hàng.</div>
            )}
          </div>

          {/* Confirm & print booking order */}
          {step === 4 && selectedSeatIds.length > 0 && (
            <div className="p-4 border-t border-black/5">
              <button
                onClick={handleCreatePOSBooking}
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-[#F5A623] text-white font-bold text-xs hover:bg-[#E09415] transition-all shadow-md shadow-[#F5A623]/25 disabled:opacity-50"
              >
                <Printer className="w-4 h-4" />
                {loading ? 'Đang tạo đơn...' : 'Chốt vé & In hóa đơn'}
              </button>
            </div>
          )}
        </div>
      </div>

      {/* POPUP HÓA ĐƠN VÉ SAU KHI ĐẶT THÀNH CÔNG ĐỂ IN RA GIẤY */}
      {printBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl p-6 w-full max-w-sm relative shadow-2xl flex flex-col">
            <button
              onClick={resetPOS}
              className="absolute top-4 right-4 w-8 h-8 rounded-full bg-black/5 flex items-center justify-center hover:bg-black/10"
            >
              <X className="w-4 h-4 text-[#6B7280]" />
            </button>

            {/* Layout in hóa đơn thực tế */}
            <div id="pos-receipt-print" className="p-4 bg-[#FAFAFA] border border-black/5 rounded-xl text-xs font-mono text-[#22232B]">
              <div className="text-center font-bold text-sm uppercase tracking-wider mb-2">StarCinema Elite</div>
              <div className="text-center mb-4">Chi nhánh: {printBooking.branchName}</div>
              <hr className="border-dashed border-black/20 my-2" />
              <div className="mb-2">Mã hóa đơn: {printBooking.bookingCode}</div>
              <div className="mb-2">Ngày tạo: {new Date(printBooking.createdAt).toLocaleString('vi-VN')}</div>
              <div className="mb-2">Nhân viên: {user?.fullName}</div>
              <hr className="border-dashed border-black/20 my-2" />
              <div className="font-bold text-xs mb-1">Vé xem phim:</div>
              <div className="pl-2 mb-2">
                <div>Phim: {printBooking.movieTitle}</div>
                <div>Suất: {printBooking.startTime.replace('T', ' ').substring(0, 16)} · {printBooking.format}</div>
                <div>Phòng chiếu: {printBooking.roomName}</div>
                <div className="font-bold text-sm">Ghế: {printBooking.seats.join(', ')}</div>
              </div>
              {printBooking.combos.length > 0 && (
                <>
                  <div className="font-bold text-xs mb-1">Dịch vụ đính kèm:</div>
                  {printBooking.combos.map((c: any) => (
                    <div key={c.comboId} className="flex justify-between pl-2">
                      <span>{c.comboName} x{c.quantity}</span>
                      <span>{fmt(c.price * c.quantity)}</span>
                    </div>
                  ))}
                </>
              )}
              <hr className="border-dashed border-black/20 my-2" />
              <div className="flex justify-between font-bold text-sm">
                <span>Tổng tiền:</span>
                <span>{fmt(printBooking.totalPrice)}</span>
              </div>
              <div className="flex justify-between mt-1">
                <span>Hình thức:</span>
                <span>{printBooking.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Quẹt thẻ'}</span>
              </div>
              <div className="text-center mt-6 text-[10px] text-[#6B7280]">StarCinema cảm ơn và hẹn gặp lại quý khách!</div>
            </div>

            <button
              onClick={() => {
                const printContent = document.getElementById('pos-receipt-print')?.innerHTML;
                const originalContent = document.body.innerHTML;
                if (printContent) {
                  document.body.innerHTML = printContent;
                  window.print();
                  document.body.innerHTML = originalContent;
                  // Reload trang để khôi phục event handlers
                  window.location.reload();
                }
              }}
              className="mt-5 w-full py-3 bg-[#F5A623] text-white font-bold text-xs rounded-xl hover:bg-[#E09415] flex items-center justify-center gap-2 shadow-lg"
            >
              <Printer className="w-4 h-4" />
              In hóa đơn ngay
            </button>
            <button
              onClick={resetPOS}
              className="mt-2 w-full py-2.5 border border-black/10 text-xs font-semibold text-[#22232B] rounded-xl hover:bg-black/5 text-center"
            >
              Đóng và tiếp tục đơn mới
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
