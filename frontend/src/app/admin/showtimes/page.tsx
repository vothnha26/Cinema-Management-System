'use client';

import { useState, useEffect, useRef } from 'react';
import { Calendar as CalendarIcon, Clock, Plus, Trash, Search, Sparkles, RefreshCw, AlertTriangle, Check, X, Eye, HelpCircle } from 'lucide-react';
import { useAuthStore } from '../../../store/authStore';
import { adminShowtimeService, adminRoomService, adminMovieService, adminBranchService, RoomData, BranchData } from '../../../services/admin';
import { hasPermission } from '../../../utils/rbac';
import api from '../../../config/api';

export default function AdminShowtimesPage() {
  const { user } = useAuthStore();
  const isAdmin = hasPermission(user?.role, 'MANAGE_USERS');
  const isManagerOrStaff = !isAdmin && hasPermission(user?.role, 'ACCESS_POS');

  const [branches, setBranches] = useState<BranchData[]>([]);
  const [selectedBranchId, setSelectedBranchId] = useState<number | ''>('');
  
  const [rooms, setRooms] = useState<any[]>([]);
  const [movies, setMovies] = useState<any[]>([]);
  const [showtimes, setShowtimes] = useState<any[]>([]);
  
  const [currentDate, setCurrentDate] = useState<string>(() => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  });

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // AI draft state
  const [aiDraftId, setAiDraftId] = useState<string | null>(null);
  const [aiDraftShowtimes, setAiDraftShowtimes] = useState<any[]>([]);
  const [aiStrategy, setAiStrategy] = useState<'MAX_REVENUE' | 'MAX_OCCUPANCY' | 'BALANCED'>('MAX_REVENUE');
  const [showAiModal, setShowAiModal] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);

  // Bulk Create state
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [bulkMovieId, setBulkMovieId] = useState<number | ''>('');
  const [bulkRoomIds, setBulkRoomIds] = useState<number[]>([]);
  const [bulkDates, setBulkDates] = useState<string[]>([]);
  const [bulkTimes, setBulkTimes] = useState<string[]>([]);
  const [bulkFormat, setBulkFormat] = useState('2D');
  
  const [newDateInput, setNewDateInput] = useState('');
  const [newTimeInput, setNewTimeInput] = useState('');
  const [bulkSubmitting, setBulkSubmitting] = useState(false);
  
  // Conflict reporting
  const [conflicts, setConflicts] = useState<any[]>([]);
  const [showConflictModal, setShowConflictModal] = useState(false);

  // Detail & Seats map state
  const [selectedShowtime, setSelectedShowtime] = useState<any | null>(null);
  const [seatStatuses, setSeatStatuses] = useState<any[]>([]);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);

  const fetchBranches = async () => {
    try {
      const res = await adminBranchService.getBranches();
      if (res?.success) {
        setBranches(res.data);
        if (isManagerOrStaff) {
          try {
            const staffRes = await api.get('/staff/me');
            if (staffRes.data?.success && staffRes.data?.data?.branch?.id) {
              setSelectedBranchId(staffRes.data.data.branch.id);
            } else if (res.data.length > 0) {
              setSelectedBranchId(res.data[0].id);
            }
          } catch {
            if (res.data.length > 0) setSelectedBranchId(res.data[0].id);
          }
        } else if (res.data.length > 0) {
          setSelectedBranchId(res.data[0].id);
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchMetadata = async () => {
    if (!selectedBranchId) return;
    try {
      const [roomRes, movieRes] = await Promise.all([
        adminRoomService.getRooms(Number(selectedBranchId)),
        adminMovieService.getMovies(),
      ]);
      if (roomRes?.success) setRooms(roomRes.data || []);
      if (movieRes?.success) setMovies(movieRes.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchShowtimes = async () => {
    if (!selectedBranchId) return;
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminShowtimeService.getShowtimes(currentDate, Number(selectedBranchId));
      if (res?.success) {
        setShowtimes(res.data || []);
      } else {
        setErrorMsg(res?.message || 'Không thể tải lịch chiếu!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi khi kết nối API.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBranches();
  }, [user]);

  useEffect(() => {
    fetchMetadata();
    fetchShowtimes();
    // Dọn dẹp bản nháp AI cũ khi đổi ngày/chi nhánh
    setAiDraftId(null);
    setAiDraftShowtimes([]);
  }, [selectedBranchId, currentDate]);

  // Điều phối AI
  const handleAISuggest = async () => {
    if (!selectedBranchId) return;
    setAiLoading(true);
    try {
      const res = await adminShowtimeService.suggestAISchedule({
        date: currentDate,
        strategy: aiStrategy,
        branchId: Number(selectedBranchId),
      });
      if (res?.success) {
        setAiDraftId(res.data.draftId);
        setAiDraftShowtimes(res.data.showtimes || []);
        setShowAiModal(false);
        alert('Gợi ý AI thành công! Bạn có thể xem trước bản nháp nét đứt trên timeline.');
      } else {
        alert(res?.message || 'Gợi ý AI thất bại!');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi khi lấy lịch gợi ý AI.');
    } finally {
      setAiLoading(false);
    }
  };

  const handleApplyAISchedule = async () => {
    if (!aiDraftId) return;
    try {
      const res = await adminShowtimeService.applyAISchedule(aiDraftId);
      if (res?.success) {
        alert('Áp dụng lịch AI thành công!');
        setAiDraftId(null);
        setAiDraftShowtimes([]);
        fetchShowtimes();
      } else {
        alert(res?.message || 'Lưu lịch AI thất bại!');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi áp dụng lịch AI.');
    }
  };

  const handleCancelAIDraft = () => {
    setAiDraftId(null);
    setAiDraftShowtimes([]);
  };

  // Tạo hàng loạt suất chiếu
  const addBulkDate = () => {
    if (!newDateInput) return;
    if (!bulkDates.includes(newDateInput)) {
      setBulkDates([...bulkDates, newDateInput]);
    }
    setNewDateInput('');
  };

  const removeBulkDate = (d: string) => {
    setBulkDates(bulkDates.filter(x => x !== d));
  };

  const addBulkTime = () => {
    if (!newTimeInput) return;
    if (!bulkTimes.includes(newTimeInput)) {
      setBulkTimes([...bulkTimes, newTimeInput]);
    }
    setNewTimeInput('');
  };

  const removeBulkTime = (t: string) => {
    setBulkTimes(bulkTimes.filter(x => x !== t));
  };

  const toggleBulkRoom = (rid: number) => {
    if (bulkRoomIds.includes(rid)) {
      setBulkRoomIds(bulkRoomIds.filter(id => id !== rid));
    } else {
      setBulkRoomIds([...bulkRoomIds, rid]);
    }
  };

  const handleBulkSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bulkMovieId || bulkRoomIds.length === 0 || bulkDates.length === 0 || bulkTimes.length === 0) {
      alert('Vui lòng điền đầy đủ thông tin: Phim, Phòng, Ngày và Giờ!');
      return;
    }

    setBulkSubmitting(true);
    try {
      const res = await adminShowtimeService.bulkCreateShowtimes({
        movieId: Number(bulkMovieId),
        roomIds: bulkRoomIds,
        dates: bulkDates,
        startTimes: bulkTimes,
        format: bulkFormat,
      });

      if (res?.success) {
        setShowBulkModal(false);
        // Reset states
        setBulkMovieId('');
        setBulkRoomIds([]);
        setBulkDates([]);
        setBulkTimes([]);
        fetchShowtimes();
        alert('Tạo suất chiếu thành công!');
      } else {
        if (res.data?.conflicts && res.data.conflicts.length > 0) {
          setConflicts(res.data.conflicts);
          setShowConflictModal(true);
        } else {
          alert(res?.message || 'Có lỗi xảy ra khi xếp lịch.');
        }
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    } finally {
      setBulkSubmitting(false);
    }
  };

  // Xem chi tiết & ghế của suất chiếu
  const handleOpenDetailShowtime = async (st: any) => {
    setSelectedShowtime(st);
    setShowDetailModal(true);
    setDetailLoading(true);
    setSeatStatuses([]);
    try {
      const res = await adminShowtimeService.getShowtimeSeats(st.id);
      if (res?.success) {
        setSeatStatuses(res.data || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleDeleteShowtime = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn hủy suất chiếu này?')) return;
    try {
      const res = await adminShowtimeService.deleteShowtime(id);
      if (res?.success) {
        setShowDetailModal(false);
        fetchShowtimes();
        alert('Đã hủy suất chiếu thành công!');
      } else {
        alert(res?.message || 'Hủy suất chiếu thất bại (Có thể vé đã được bán).');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi hủy suất chiếu.');
    }
  };

  // Timeline render helpers
  const hours = Array.from({ length: 17 }, (_, i) => i + 8); // Từ 08:00 đến 24:00
  const hourWidth = 140; // Độ rộng mỗi giờ (px)
  const timelineStartHour = 8;

  const calculatePosition = (timeStr: string, durationMin: number) => {
    const [h, m] = timeStr.split(':').map(Number);
    const totalMinutesFromStart = (h - timelineStartHour) * 60 + m;
    const left = (totalMinutesFromStart / 60) * hourWidth + 60; // offset left 60px cho cột phòng
    const width = (durationMin / 60) * hourWidth;
    return { left, width };
  };

  const renderSeatMapDetail = () => {
    if (detailLoading) return <div className="text-center py-6 text-sm text-[#6B7280]">Đang tải sơ đồ ghế...</div>;
    if (seatStatuses.length === 0) return <div className="text-center py-6 text-sm text-[#6B7280]">Không có dữ liệu ghế.</div>;

    const rowsCount = selectedShowtime?.roomRows || 1;
    const colsCount = selectedShowtime?.roomCols || 1;

    // Sắp xếp ghế theo hàng cột
    const sortedSeats = [...seatStatuses].sort((a, b) => {
      if (a.rowChar !== b.rowChar) return a.rowChar.localeCompare(b.rowChar);
      return a.colNum - b.colNum;
    });

    return (
      <div className="bg-[#FAFAFA] border border-black/5 rounded-2xl p-6 flex flex-col items-center overflow-x-auto max-w-full">
        {/* Screen simulator */}
        <div className="w-64 h-1 bg-black/10 rounded-full mb-8 relative">
          <span className="absolute top-2 left-1/2 -translate-x-1/2 text-[8px] font-extrabold text-[#6B7280] tracking-widest whitespace-nowrap uppercase">
            Màn hình
          </span>
        </div>

        <div className="grid gap-1.5" style={{ gridTemplateColumns: `repeat(${colsCount}, minmax(0, 1fr))` }}>
          {sortedSeats.map((s: any, idx: number) => {
            let seatColor = 'bg-white border-black/10 text-[#6B7280]'; // Available
            if (s.seatTypeId === 'VIP') seatColor = 'bg-[#F5A623]/10 border-[#F5A623] text-[#F5A623]';
            if (s.seatTypeId === 'COUPLE') seatColor = 'bg-[#9B59B6]/10 border-[#9B59B6] text-[#9B59B6]';
            
            // Trạng thái đã bán hoặc đã đặt chỗ
            if (s.status === 'SOLD' || s.status === 'LOCKED' || s.sold) {
              seatColor = 'bg-[#EF4444] border-[#EF4444] text-white';
            }
            if (s.seatTypeId === 'EMPTY') {
              seatColor = 'bg-transparent border-transparent text-transparent';
            }

            return (
              <div
                key={idx}
                className={`w-7 h-7 rounded border flex items-center justify-center text-[8px] font-bold ${seatColor}`}
                title={`${s.rowChar}${s.colNum} (${s.status || 'AVAILABLE'})`}
              >
                {s.seatTypeId === 'EMPTY' ? '' : `${s.rowChar}${s.colNum}`}
              </div>
            );
          })}
        </div>

        <div className="flex items-center gap-4 mt-6 text-[10px] font-semibold text-[#6B7280] flex-wrap justify-center border-t border-black/5 pt-4 w-full">
          <div className="flex items-center gap-1"><div className="w-3 h-3 bg-white border border-black/10 rounded" /> Trống</div>
          <div className="flex items-center gap-1"><div className="w-3 h-3 bg-[#F5A623]/25 border border-[#F5A623] rounded" /> VIP</div>
          <div className="flex items-center gap-1"><div className="w-3 h-3 bg-[#9B59B6]/25 border border-[#9B59B6] rounded" /> Đôi</div>
          <div className="flex items-center gap-1"><div className="w-3 h-3 bg-[#EF4444] rounded" /> Đã bán</div>
        </div>
      </div>
    );
  };

  // Trộn danh sách showtimes thực và nháp AI để render
  const combinedShowtimes = [
    ...showtimes.map(s => ({ ...s, isDraft: false })),
    ...aiDraftShowtimes.map(s => ({
      ...s,
      isDraft: true,
      id: `draft-${s.id || Math.random()}`,
      movieTitle: movies.find(m => m.id === s.movieId)?.title || 'Phim gợi ý',
      durationMinutes: movies.find(m => m.id === s.movieId)?.durationMinutes || 120,
    })),
  ];

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Clock className="w-6 h-6 text-[#F5A623]" />
            Điều phối Suất Chiếu
          </h1>
          <p className="text-sm text-[#6B7280]">
            Timeline điều phối lịch chiếu phim, tích hợp thuật toán gợi ý AI tối ưu hóa doanh thu
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold text-[#6B7280]">Chi nhánh:</span>
            <select
              value={selectedBranchId}
              onChange={(e) => setSelectedBranchId(Number(e.target.value))}
              disabled={isManagerOrStaff}
              className="px-3 py-2 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            >
              {branches.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2">
            <CalendarIcon className="w-4 h-4 text-[#6B7280]" />
            <input
              type="date"
              value={currentDate}
              onChange={(e) => setCurrentDate(e.target.value)}
              className="px-3 py-2 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            />
          </div>

          <button
            onClick={() => { fetchShowtimes(); handleCancelAIDraft(); }}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          {isAdmin && (
            <>
              <button
                onClick={() => setShowAiModal(true)}
                className="px-4 py-2.5 bg-gradient-to-r from-indigo-600 to-indigo-700 text-white rounded-xl font-bold hover:opacity-90 transition-all flex items-center gap-1.5 shadow-md shadow-indigo-600/20 active:scale-[0.98]"
              >
                <Sparkles className="w-4 h-4" />
                AI Draft
              </button>

              <button
                onClick={() => setShowBulkModal(true)}
                className="px-4 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-1.5 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
              >
                <Plus className="w-4 h-4" />
                Tạo Hàng Loạt
              </button>
            </>
          )}
        </div>
      </div>

      {/* AI Draft control row if draft exists */}
      {aiDraftId && (
        <div className="bg-indigo-50 border border-indigo-200 text-indigo-800 px-5 py-4 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-4 animate-in slide-in-from-top-4 duration-300">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-indigo-600 animate-pulse" />
            <div className="text-sm">
              <span className="font-bold">Lịch chiếu gợi ý bởi AI đang ở chế độ xem trước!</span> Bạn có thể lưu chính thức hoặc hủy bỏ bản nháp này bất cứ lúc nào.
            </div>
          </div>
          <div className="flex items-center gap-2 w-full md:w-auto">
            <button
              onClick={handleApplyAISchedule}
              className="flex-1 md:flex-none px-4 py-2 bg-indigo-600 text-white text-xs font-bold rounded-xl hover:bg-indigo-700 transition-colors flex items-center justify-center gap-1 shadow-sm"
            >
              <Check className="w-4 h-4" /> Áp dụng chính thức
            </button>
            <button
              onClick={handleCancelAIDraft}
              className="flex-1 md:flex-none px-4 py-2 bg-white border border-indigo-200 text-indigo-700 text-xs font-bold rounded-xl hover:bg-indigo-100 transition-colors flex items-center justify-center gap-1"
            >
              <X className="w-4 h-4" /> Hủy bỏ bản nháp
            </button>
          </div>
        </div>
      )}

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* TIMELINE SCHEDULER */}
      <div className="bg-white border border-black/5 rounded-2xl shadow-sm overflow-hidden flex flex-col">
        {/* Timeline Header (Giờ) */}
        <div className="flex border-b border-black/5 bg-[#FAFAFA]" style={{ minWidth: `${hourWidth * hours.length + 180}px` }}>
          <div className="w-[180px] p-4 font-bold text-xs text-[#6B7280] uppercase tracking-wider sticky left-0 bg-[#FAFAFA] border-r border-black/5 z-20">
            Phòng Chiếu
          </div>
          <div className="flex-1 flex relative h-12">
            {hours.map((hour) => (
              <div
                key={hour}
                className="absolute top-0 bottom-0 border-l border-black/5 flex items-center pl-2 text-xs font-bold text-[#6B7280]"
                style={{ left: `${(hour - timelineStartHour) * hourWidth}px`, width: `${hourWidth}px` }}
              >
                {hour < 10 ? `0${hour}` : hour}:00
              </div>
            ))}
          </div>
        </div>

        {/* Timeline Rooms & blocks */}
        <div className="flex-1 overflow-x-auto divide-y divide-black/5" style={{ minWidth: `${hourWidth * hours.length + 180}px` }}>
          {rooms.length === 0 ? (
            <div className="py-12 text-center text-sm font-semibold text-[#6B7280]">
              Chi nhánh này chưa thiết lập phòng chiếu nào.
            </div>
          ) : (
            rooms.map((room) => {
              const roomShowtimes = combinedShowtimes.filter((s) => s.roomId === room.id);

              return (
                <div key={room.id} className="flex relative h-20 group hover:bg-[#FAFAFA]/50 transition-colors">
                  {/* Left col - Room info */}
                  <div className="w-[180px] p-4 sticky left-0 bg-white group-hover:bg-[#FAFAFA]/50 border-r border-black/5 z-10 flex flex-col justify-center">
                    <span className="font-bold text-sm text-[#22232B] line-clamp-1">{room.name}</span>
                    <span className="text-[10px] font-bold text-[#6B7280] uppercase tracking-wider">{room.roomTypeId?.replace('HALL_', '')}</span>
                  </div>

                  {/* Right - Blocks container */}
                  <div className="flex-1 relative h-full">
                    {/* Vẽ lưới giờ mờ */}
                    {hours.map((hour) => (
                      <div
                        key={hour}
                        className="absolute top-0 bottom-0 border-l border-black/[0.03]"
                        style={{ left: `${(hour - timelineStartHour) * hourWidth}px`, width: `${hourWidth}px` }}
                      />
                    ))}

                    {/* Danh sách block suất chiếu */}
                    {roomShowtimes.map((st: any) => {
                      const timeStr = st.startTime || '08:00';
                      const duration = st.durationMinutes || 120;
                      const { left, width } = calculatePosition(timeStr, duration);

                      // Style khác biệt giữa chính thức và draft
                      let blockCls = 'bg-gradient-to-r from-amber-500 to-amber-600 shadow-md shadow-amber-500/10 text-white';
                      if (st.isDraft) {
                        blockCls = 'border-2 border-dashed border-indigo-500 bg-indigo-50 text-indigo-800 shadow-sm';
                      }

                      return (
                        <div
                          key={st.id}
                          onClick={() => !st.isDraft && handleOpenDetailShowtime(st)}
                          style={{ left: `${left}px`, width: `${width}px` }}
                          className={`absolute top-3 bottom-3 rounded-xl px-3 py-2 text-xs flex flex-col justify-between cursor-pointer transition-all hover:scale-[1.02] hover:-translate-y-0.5 hover:shadow-lg ${blockCls}`}
                        >
                          <div className="font-bold line-clamp-1 leading-snug">{st.movieTitle}</div>
                          <div className="flex items-center justify-between text-[10px] font-semibold opacity-90">
                            <span className="flex items-center gap-0.5">
                              <Clock className="w-3 h-3" /> {timeStr}
                            </span>
                            <span>{duration}m</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* Modal AI Scheduling */}
      {showAiModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">Điều phối AI Draft</h3>
              <button onClick={() => setShowAiModal(false)} className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280]"><X className="w-4 h-4" /></button>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-xs text-[#6B7280] leading-relaxed">
                Hệ thống AI sẽ tự động điều phối, tính toán khoảng cách vệ sinh và thời lượng phim để tạo lịch chiếu tối ưu nhất cho chi nhánh.
              </p>
              <div>
                <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Chiến lược tối ưu</label>
                <select
                  value={aiStrategy}
                  onChange={(e) => setAiStrategy(e.target.value as any)}
                  className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                >
                  <option value="MAX_REVENUE">Tối đa doanh thu (Max Revenue)</option>
                  <option value="MAX_OCCUPANCY">Tối đa tỷ lệ lấp đầy (Max Occupancy)</option>
                  <option value="BALANCED">Cân bằng tối ưu (Balanced)</option>
                </select>
              </div>
            </div>
            <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-end gap-3">
              <button onClick={() => setShowAiModal(false)} className="px-4 py-2 border border-black/10 rounded-xl font-semibold text-[#6B7280] text-sm">Hủy bỏ</button>
              <button
                onClick={handleAISuggest}
                disabled={aiLoading}
                className="px-4 py-2 bg-indigo-600 text-white rounded-xl font-bold hover:bg-indigo-700 transition-colors flex items-center gap-1 text-sm shadow-md shadow-indigo-600/25 disabled:opacity-50"
              >
                <Sparkles className="w-4 h-4" />
                {aiLoading ? 'Đang phân tích...' : 'Gợi ý lịch'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Tạo Hàng Loạt (Bulk Create) */}
      {showBulkModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-black/5 overflow-hidden my-8 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">Tạo Suất Chiếu Hàng Loạt</h3>
              <button onClick={() => setShowBulkModal(false)} className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280]"><X className="w-4 h-4" /></button>
            </div>

            <form onSubmit={handleBulkSubmit}>
              <div className="p-6 space-y-5 max-h-[70vh] overflow-y-auto">
                {/* Chọn phim */}
                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Bộ phim chiếu</label>
                  <select
                    value={bulkMovieId}
                    onChange={(e) => setBulkMovieId(Number(e.target.value))}
                    required
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                  >
                    <option value="">-- Chọn phim --</option>
                    {movies.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.title} ({m.durationMinutes} phút)
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  {/* Định dạng */}
                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Định dạng</label>
                    <select
                      value={bulkFormat}
                      onChange={(e) => setBulkFormat(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                    >
                      <option value="2D">2D</option>
                      <option value="3D">3D</option>
                      <option value="IMAX">IMAX</option>
                      <option value="4DX">4DX</option>
                    </select>
                  </div>
                </div>

                {/* Chọn phòng */}
                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Chọn Phòng Chiếu (Có thể chọn nhiều)</label>
                  <div className="flex flex-wrap gap-2">
                    {rooms.map((room) => {
                      const selected = bulkRoomIds.includes(room.id);
                      return (
                        <button
                          key={room.id}
                          type="button"
                          onClick={() => toggleBulkRoom(room.id)}
                          className={`px-4 py-2.5 rounded-xl text-xs font-bold border transition-all ${
                            selected
                              ? 'bg-[#F5A623] border-[#F5A623] text-white'
                              : 'bg-white border-black/10 text-[#6B7280] hover:bg-black/5'
                          }`}
                        >
                          {room.name}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Chọn ngày */}
                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Các ngày chiếu áp dụng</label>
                  <div className="flex gap-2">
                    <input
                      type="date"
                      value={newDateInput}
                      onChange={(e) => setNewDateInput(e.target.value)}
                      className="flex-1 px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                    />
                    <button
                      type="button"
                      onClick={addBulkDate}
                      className="px-4 py-2.5 bg-black text-white text-xs font-bold rounded-xl"
                    >
                      Thêm ngày
                    </button>
                  </div>
                  {bulkDates.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-3 bg-[#FAFAFA] border border-black/5 p-3 rounded-xl">
                      {bulkDates.map((d) => (
                        <span key={d} className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-[#F5A623]/10 text-[#F5A623]">
                          {d}
                          <X className="w-3.5 h-3.5 cursor-pointer" onClick={() => removeBulkDate(d)} />
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                {/* Chọn giờ khởi chiếu */}
                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giờ khởi chiếu (Suất chiếu)</label>
                  <div className="flex gap-2">
                    <input
                      type="time"
                      value={newTimeInput}
                      onChange={(e) => setNewTimeInput(e.target.value)}
                      className="flex-1 px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                    />
                    <button
                      type="button"
                      onClick={addBulkTime}
                      className="px-4 py-2.5 bg-black text-white text-xs font-bold rounded-xl"
                    >
                      Thêm giờ
                    </button>
                  </div>
                  {bulkTimes.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-3 bg-[#FAFAFA] border border-black/5 p-3 rounded-xl">
                      {bulkTimes.map((t) => (
                        <span key={t} className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-black/5 text-[#6B7280]">
                          {t}
                          <X className="w-3.5 h-3.5 cursor-pointer" onClick={() => removeBulkTime(t)} />
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Footer */}
              <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowBulkModal(false)}
                  className="px-4 py-2.5 border border-black/10 rounded-xl font-semibold text-[#6B7280] text-sm"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={bulkSubmitting}
                  className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-1.5 shadow-md shadow-[#F5A623]/25 active:scale-[0.98] disabled:opacity-50 text-sm"
                >
                  <Plus className="w-4 h-4" />
                  {bulkSubmitting ? 'Đang xử lý...' : 'Xếp lịch hàng loạt'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Báo Cáo Xung Đột Lịch Chiếu (Conflict report modal) */}
      {showConflictModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center gap-2 px-6 py-4 border-b border-black/5 bg-red-50 text-red-800">
              <AlertTriangle className="w-5 h-5 text-red-600" />
              <h3 className="font-bold text-base">Xung Đột Lịch Trùng Phòng Chiếu</h3>
            </div>
            <div className="p-6 space-y-4 max-h-[50vh] overflow-y-auto">
              <p className="text-xs text-[#6B7280]">
                Không thể xếp lịch cho các khung giờ dưới đây do trùng phòng và thời gian với lịch chiếu sẵn có:
              </p>
              <div className="space-y-2">
                {conflicts.map((c, idx) => (
                  <div key={idx} className="bg-red-50/55 border border-red-100 rounded-xl p-3 text-xs text-red-900">
                    <div className="font-bold">Ngày: {c.date} | Phòng: {c.roomName}</div>
                    <div className="mt-1 opacity-90">Giờ đề xuất: <span className="font-bold">{c.requestedStartTime}</span> trùng với suất chiếu bộ phim: <span className="font-bold">{c.conflictingMovieTitle}</span> ({c.conflictingStartTime} - {c.conflictingEndTime})</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-end">
              <button
                onClick={() => setShowConflictModal(false)}
                className="px-5 py-2.5 bg-black text-white rounded-xl font-bold text-xs"
              >
                Đồng ý / Đóng lại
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Chi Tiết Suất Chiếu & Sơ Đồ Ghế Real-time */}
      {showDetailModal && selectedShowtime && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-xl w-full shadow-2xl border border-black/5 overflow-hidden my-8 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <div>
                <h3 className="font-bold text-base text-[#22232B]">{selectedShowtime.movieTitle}</h3>
                <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-wider mt-0.5">Chi tiết suất chiếu #{selectedShowtime.id}</p>
              </div>
              <button onClick={() => setShowDetailModal(false)} className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280]"><X className="w-4 h-4" /></button>
            </div>

            <div className="p-6 space-y-6 max-h-[70vh] overflow-y-auto">
              {/* Info grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs font-semibold text-[#6B7280] bg-[#FAFAFA] border border-black/5 p-4 rounded-xl">
                <div>
                  <span className="block text-[9px] uppercase tracking-wider text-[#6B7280]/60 mb-1">Giờ bắt đầu</span>
                  <span className="text-[#22232B] text-sm font-bold flex items-center gap-1"><Clock className="w-4 h-4 text-[#F5A623]" /> {selectedShowtime.startTime}</span>
                </div>
                <div>
                  <span className="block text-[9px] uppercase tracking-wider text-[#6B7280]/60 mb-1">Phòng chiếu</span>
                  <span className="text-[#22232B] text-sm font-bold">{selectedShowtime.roomName}</span>
                </div>
                <div>
                  <span className="block text-[9px] uppercase tracking-wider text-[#6B7280]/60 mb-1">Định dạng</span>
                  <span className="text-[#22232B] text-sm font-bold">{selectedShowtime.format || '2D'}</span>
                </div>
                <div>
                  <span className="block text-[9px] uppercase tracking-wider text-[#6B7280]/60 mb-1">Thời lượng</span>
                  <span className="text-[#22232B] text-sm font-bold">{selectedShowtime.durationMinutes} phút</span>
                </div>
              </div>

              {/* Sơ đồ ghế thực tế */}
              <div>
                <label className="block text-sm font-bold text-[#22232B] mb-3">Sơ Đồ Ghế Ngồi (Thời gian thực)</label>
                {renderSeatMapDetail()}
              </div>
            </div>

            {/* Footer */}
            <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-between gap-3">
              {isAdmin ? (
                <button
                  type="button"
                  onClick={() => handleDeleteShowtime(selectedShowtime.id)}
                  className="px-4 py-2 border border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 rounded-xl font-bold text-xs flex items-center gap-1.5"
                >
                  <Trash className="w-3.5 h-3.5" /> Hủy suất chiếu
                </button>
              ) : <div />}
              
              <button
                type="button"
                onClick={() => setShowDetailModal(false)}
                className="px-4 py-2 bg-black text-white rounded-xl font-bold text-xs"
              >
                Đóng lại
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
