'use client';

import { use, useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Save, Sparkles, RefreshCw, Grid, Layers, Trash, Check, CheckSquare } from 'lucide-react';
import { adminRoomService, RoomData } from '../../../../../services/admin';
import { useAuthStore } from '../../../../../store/authStore';
import { hasPermission } from '../../../../../utils/rbac';

export default function RoomDesignPage({ params }: { params: Promise<{ id: string }> }) {
  const { id: roomIdStr } = use(params);
  const roomId = Number(roomIdStr);
  const router = useRouter();
  
  const { user } = useAuthStore();
  const isAdmin = hasPermission(user?.role, 'MANAGE_USERS'); // Admin mới có quyền thiết kế

  const [roomData, setRoomData] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  
  const [selectedIndices, setSelectedIndices] = useState<Set<string>>(new Set());
  const [isMouseDown, setIsMouseDown] = useState(false);
  
  // Layout values
  const [rows, setRows] = useState<number>(10);
  const [cols, setCols] = useState<number>(10);
  
  // Templates
  const [templates, setTemplates] = useState<string[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState('');

  const loadRoomDetail = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminRoomService.getRoomDetails(roomId);
      if (res.success) {
        setRoomData(res.data);
        setRows(res.data.rows || 10);
        setCols(res.data.cols || 10);
      } else {
        setErrorMsg(res.message || 'Không thể lấy thông tin phòng chiếu.');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  const loadTemplates = async () => {
    try {
      const res = await adminRoomService.getRoomTemplates();
      if (res.success) {
        setTemplates(res.data || []);
      }
    } catch (err) {
      console.error('Không thể lấy danh sách templates:', err);
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      alert('Bạn không có quyền truy cập module thiết kế sơ đồ phòng!');
      router.push('/admin/rooms');
      return;
    }
    loadRoomDetail();
    loadTemplates();

    // Mouse handlers to facilitate drag-to-select
    const handleMouseUp = () => setIsMouseDown(false);
    window.addEventListener('mouseup', handleMouseUp);
    return () => window.removeEventListener('mouseup', handleMouseUp);
  }, [roomId, user]);

  const adjustGrid = () => {
    if (rows > 26 || cols > 30) {
      alert('Kích thước phòng chiếu tối đa là 26 hàng và 30 cột!');
      return;
    }
    if (roomData) {
      setRoomData({
        ...roomData,
        rows,
        cols,
      });
    }
  };

  const applyTemplate = async () => {
    if (!selectedTemplate) return;
    try {
      const res = await adminRoomService.getTemplateDetails(selectedTemplate);
      if (res.success) {
        const temp = res.data;
        setRows(temp.rows);
        setCols(temp.cols);
        setRoomData({
          ...roomData,
          rows: temp.rows,
          cols: temp.cols,
          seats: temp.seats.map((s: any) => ({
            rowChar: s.rowChar,
            colNum: s.colNum,
            seatTypeId: s.seatTypeId,
            status: s.status,
          })),
        });
        setSelectedIndices(new Set());
      }
    } catch (err) {
      alert('Không thể tải template sơ đồ ghế!');
    }
  };

  const exportAsTemplate = async () => {
    const templateName = prompt('Nhập tên mẫu sơ đồ ghế (Template Name) mới:');
    if (!templateName) return;
    
    const activeSeats = (roomData?.seats || []).filter((s: any) => s.seatTypeId !== 'EMPTY');
    const data = {
      templateName,
      roomTypeId: roomData?.roomTypeId || 'HALL_2D',
      rows: roomData?.rows || rows,
      cols: roomData?.cols || cols,
      seats: activeSeats.map((s: any) => ({
        rowChar: s.rowChar,
        colNum: s.colNum,
        seatTypeId: s.seatTypeId,
        status: s.status,
      })),
    };

    try {
      const res = await adminRoomService.saveTemplate(data);
      if (res.success) {
        alert('Đã lưu mẫu sơ đồ ghế thành công!');
        loadTemplates();
      } else {
        alert(res.message || 'Lưu thất bại!');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi khi lưu mẫu sơ đồ ghế.');
    }
  };

  const toggleSelect = (idx: string, forceAdd = false) => {
    setSelectedIndices((prev) => {
      const next = new Set(prev);
      if (!forceAdd && next.has(idx)) {
        next.delete(idx);
      } else {
        next.add(idx);
      }
      return next;
    });
  };

  const selectRowPrompt = () => {
    const rowInput = prompt('Nhập tên hàng cần chọn (A, B, C...):');
    if (!rowInput) return;
    const rowChar = rowInput.toUpperCase();
    for (let c = 1; c <= cols; c++) {
      toggleSelect(`${rowChar}-${c}`, true);
    }
  };

  const selectColPrompt = () => {
    const colInput = prompt(`Nhập số cột cần chọn (1 - ${cols}):`);
    if (!colInput) return;
    const colNum = parseInt(colInput);
    if (isNaN(colNum) || colNum < 1 || colNum > cols) return;
    for (let r = 0; r < rows; r++) {
      const rowChar = String.fromCharCode(65 + r);
      toggleSelect(`${rowChar}-${colNum}`, true);
    }
  };

  const selectAllSeats = () => {
    for (let r = 0; r < rows; r++) {
      const rowChar = String.fromCharCode(65 + r);
      for (let c = 1; c <= cols; c++) {
        toggleSelect(`${rowChar}-${c}`, true);
      }
    }
  };

  const clearAllSeats = () => {
    if (!confirm('Bạn có chắc muốn dọn trắng sơ đồ (xóa toàn bộ ghế)?')) return;
    if (roomData) {
      setRoomData({
        ...roomData,
        seats: [],
      });
      setSelectedIndices(new Set());
    }
  };

  const applyType = (type: 'STANDARD' | 'VIP' | 'COUPLE' | 'EMPTY') => {
    if (selectedIndices.size === 0) {
      alert('Vui lòng chọn ít nhất một ô trên lưới để áp dụng!');
      return;
    }

    if (roomData) {
      const updatedSeats = [...(roomData.seats || [])];
      
      selectedIndices.forEach((idx) => {
        const [r, c] = idx.split('-');
        const colNum = parseInt(c);
        let seatIdx = updatedSeats.findIndex((s) => s.rowChar === r && s.colNum === colNum);
        
        if (seatIdx === -1) {
          updatedSeats.push({
            rowChar: r,
            colNum: colNum,
            seatTypeId: type,
            status: type !== 'EMPTY',
          });
        } else {
          updatedSeats[seatIdx] = {
            ...updatedSeats[seatIdx],
            seatTypeId: type,
            status: type !== 'EMPTY',
          };
        }
      });

      setRoomData({
        ...roomData,
        seats: updatedSeats,
      });
      
      setSelectedIndices(new Set());
    }
  };

  const saveLayout = async () => {
    if (!roomData) return;
    
    // Đảm bảo toàn bộ ma trận ghế được gửi đi
    const allSeatsForGrid = [];
    for (let r = 0; r < rows; r++) {
      const rowChar = String.fromCharCode(65 + r);
      for (let c = 1; c <= cols; c++) {
        const seat = (roomData.seats || []).find((s: any) => s.rowChar === rowChar && s.colNum === c);
        allSeatsForGrid.push({
          rowChar: rowChar,
          colNum: c,
          seatTypeId: seat ? seat.seatTypeId : 'EMPTY',
          status: seat ? seat.seatTypeId !== 'EMPTY' : false,
        });
      }
    }

    try {
      const res = await adminRoomService.updateRoomLayout(roomId, {
        rows,
        cols,
        seats: allSeatsForGrid,
      });
      if (res.success) {
        alert('Đã lưu sơ đồ phòng và số lượng ghế thành công!');
        loadRoomDetail();
      } else {
        alert(res.message || 'Lưu sơ đồ thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu sơ đồ.');
    }
  };

  // Helper stats
  const totalSeats = (roomData?.seats || []).filter((s: any) => s.seatTypeId !== 'EMPTY').length;
  const vipSeats = (roomData?.seats || []).filter((s: any) => s.seatTypeId === 'VIP').length;
  const coupleSeats = (roomData?.seats || []).filter((s: any) => s.seatTypeId === 'COUPLE').length;

  return (
    <div className="fixed inset-0 z-50 bg-[#080B14] text-[#E8EAF0] flex overflow-hidden font-sans">
      {/* Cột cài đặt bên trái */}
      <aside className="w-80 bg-[#0F1320] border-r border-[#252A3D] p-6 flex flex-col justify-between shrink-0 h-full z-10">
        <div className="space-y-6">
          <Link href="/admin/rooms" className="inline-flex items-center gap-2 text-sm text-[#7B82A0] hover:text-white transition-colors">
            <ArrowLeft className="w-4 h-4" />
            Quay lại quản lý phòng
          </Link>

          <div>
            <h2 className="text-xl font-bold text-[#F5A623] tracking-wide" id="roomNameLabel">
              {roomData?.name || 'Đang tải...'}
            </h2>
            <p className="text-xs text-[#7B82A0] mt-1 uppercase font-bold tracking-wider">
              Thiết Kế Sơ Đồ Ghế
            </p>
          </div>

          {/* Thiết lập kích thước Grid */}
          <div className="space-y-2">
            <label className="block text-[10px] font-bold tracking-wider text-[#7B82A0] uppercase">Kích thước lưới</label>
            <div className="grid grid-cols-2 gap-2">
              <div>
                <span className="text-[10px] text-[#7B82A0]">Hàng (A-Z)</span>
                <input
                  type="number"
                  min="1"
                  max="26"
                  value={rows}
                  onChange={(e) => setRows(Number(e.target.value))}
                  className="w-full bg-[#161B2E] border border-[#252A3D] rounded-xl px-3 py-2 text-sm font-semibold outline-none focus:border-[#E5133A] text-white"
                />
              </div>
              <div>
                <span className="text-[10px] text-[#7B82A0]">Cột (1-30)</span>
                <input
                  type="number"
                  min="1"
                  max="30"
                  value={cols}
                  onChange={(e) => setCols(Number(e.target.value))}
                  className="w-full bg-[#161B2E] border border-[#252A3D] rounded-xl px-3 py-2 text-sm font-semibold outline-none focus:border-[#E5133A] text-white"
                />
              </div>
            </div>
            <button
              onClick={adjustGrid}
              className="w-full mt-2 py-2 rounded-xl border border-[#252A3D] text-xs font-bold hover:bg-white/5 active:scale-[0.98] transition-all text-white"
            >
              Cập nhật kích thước lưới
            </button>
          </div>

          {/* Thư viện template mẫu */}
          <div className="space-y-2">
            <label className="block text-[10px] font-bold tracking-wider text-[#7B82A0] uppercase">Thư viện mẫu (Templates)</label>
            <select
              value={selectedTemplate}
              onChange={(e) => setSelectedTemplate(e.target.value)}
              className="w-full bg-[#161B2E] border border-[#252A3D] rounded-xl px-3 py-2.5 text-sm font-semibold outline-none focus:border-[#E5133A] text-white"
            >
              <option value="">-- Chọn Template mẫu --</option>
              {templates.map((name) => (
                <option key={name} value={name}>
                  {name.replace('.json', '').replace(/_/g, ' ')}
                </option>
              ))}
            </select>
            <div className="grid grid-cols-2 gap-2 mt-1">
              <button
                onClick={applyTemplate}
                className="py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-[10px] font-bold active:scale-[0.98] transition-all"
              >
                Áp dụng mẫu
              </button>
              <button
                onClick={exportAsTemplate}
                className="py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-[10px] font-bold active:scale-[0.98] transition-all"
              >
                Lưu thành mẫu
              </button>
            </div>
          </div>
        </div>

        {/* Stats card */}
        <div className="bg-[#161B2E]/60 border border-[#252A3D] rounded-2xl p-4 mt-6 space-y-3">
          <div className="text-[10px] font-bold tracking-wider text-[#7B82A0] uppercase mb-1">Thống kê ghế</div>
          <div className="flex justify-between text-xs font-semibold">
            <span className="text-[#E8EAF0]">Tổng số ghế:</span>
            <span className="text-white font-bold">{totalSeats}</span>
          </div>
          <div className="flex justify-between text-xs font-semibold">
            <span className="text-[#F5A623]">Ghế VIP:</span>
            <span className="text-[#F5A623] font-bold">{vipSeats}</span>
          </div>
          <div className="flex justify-between text-xs font-semibold">
            <span className="text-[#9B59B6]">Ghế Đôi:</span>
            <span className="text-[#9B59B6] font-bold">{coupleSeats}</span>
          </div>
          
          <hr className="border-[#252A3D] my-2" />
          
          <div className="text-xs font-bold text-[#F5A623]">
            {selectedIndices.size} ghế được chọn
          </div>
          <p className="text-[10px] text-[#7B82A0] leading-relaxed">
            Nhấp chuột hoặc nhấn giữ kéo chuột trên lưới để chọn vùng ghế cần thiết kế.
          </p>
        </div>
      </aside>

      {/* Vùng Canvas thiết kế ở giữa */}
      <main className="flex-1 overflow-auto flex flex-col items-center justify-start py-10 px-6 relative bg-[radial-gradient(circle_at_center,#111526_0%,#080b14_100%)]">
        {/* Quick select tool row */}
        <div className="flex items-center gap-2 mb-6 bg-[#0F1320] border border-[#252A3D] rounded-2xl p-2 shadow-xl">
          <button
            onClick={selectRowPrompt}
            className="px-3.5 py-1.5 rounded-xl hover:bg-[#161B2E] text-xs font-bold text-[#E8EAF0] active:scale-[0.97] transition-all"
          >
            Chọn hàng
          </button>
          <button
            onClick={selectColPrompt}
            className="px-3.5 py-1.5 rounded-xl hover:bg-[#161B2E] text-xs font-bold text-[#E8EAF0] active:scale-[0.97] transition-all"
          >
            Chọn cột
          </button>
          <button
            onClick={selectAllSeats}
            className="px-3.5 py-1.5 rounded-xl hover:bg-[#161B2E] text-xs font-bold text-[#E8EAF0] active:scale-[0.97] transition-all flex items-center gap-1"
          >
            <CheckSquare className="w-3.5 h-3.5" />
            Chọn tất cả
          </button>
          <div className="w-[1px] h-4 bg-[#252A3D]" />
          <button
            onClick={clearAllSeats}
            className="px-3.5 py-1.5 rounded-xl hover:bg-red-500/10 text-xs font-bold text-red-500 active:scale-[0.97] transition-all flex items-center gap-1"
          >
            <Trash className="w-3.5 h-3.5" />
            Xóa trắng
          </button>
        </div>

        {loading ? (
          <div className="text-center py-20 text-[#7B82A0] font-semibold flex flex-col items-center gap-3">
            <RefreshCw className="w-8 h-8 animate-spin text-[#F5A623]" />
            Đang tải sơ đồ thiết kế...
          </div>
        ) : errorMsg ? (
          <div className="text-red-500 font-semibold py-20">{errorMsg}</div>
        ) : (
          <div className="bg-white/[0.02] border border-[#252A3D] rounded-3xl p-12 flex flex-col items-center shadow-2xl relative min-w-max">
            {/* Simulation screen banner */}
            <div className="w-96 h-1.5 bg-[#E5133A] rounded-full shadow-[0_4px_25px_rgba(229,19,58,0.6)] mb-16 relative">
              <span className="absolute top-4 left-1/2 -translate-y-1/2 -translate-x-1/2 text-[9px] font-extrabold tracking-[0.4em] text-[#7B82A0] uppercase whitespace-nowrap">
                Màn hình chính
              </span>
            </div>

            {/* Lưới sơ đồ ghế chính */}
            <div
              className="grid gap-2 select-none"
              style={{ gridTemplateColumns: `30px repeat(${cols}, minmax(0, 1fr))` }}
              onMouseDown={() => setIsMouseDown(true)}
            >
              {Array.from({ length: rows }).map((_, rIdx) => {
                const rowChar = String.fromCharCode(65 + rIdx);
                
                return (
                  <div key={rowChar} className="contents">
                    {/* Nhãn hàng ghế bên trái */}
                    <div className="h-9 flex items-center justify-center text-xs font-bold text-[#7B82A0] select-none">
                      {rowChar}
                    </div>

                    {/* Danh sách ghế trong hàng */}
                    {Array.from({ length: cols }).map((_, colIdx) => {
                      const colNum = colIdx + 1;
                      const seatIdx = `${rowChar}-${colNum}`;
                      const seat = (roomData?.seats || []).find((s: any) => s.rowChar === rowChar && s.colNum === colNum);
                      const type = seat ? seat.seatTypeId : 'EMPTY';
                      
                      let seatClass = 'bg-[#161B2E] border-[#252A3D] text-[#7B82A0]'; // standard
                      if (type === 'VIP') seatClass = 'bg-[#F5A623]/10 border-[#F5A623] text-[#F5A623]';
                      if (type === 'COUPLE') seatClass = 'bg-[#9B59B6]/10 border-[#9B59B6] text-[#9B59B6] col-span-2 w-[80px]';
                      if (type === 'EMPTY') seatClass = 'bg-transparent border-dashed border-[#252A3D] text-transparent hover:border-white/20';

                      const isSelected = selectedIndices.has(seatIdx);

                      return (
                        <div
                          key={seatIdx}
                          id={`cell-${seatIdx}`}
                          onMouseDown={(e) => {
                            e.preventDefault();
                            toggleSelect(seatIdx);
                          }}
                          onMouseEnter={() => {
                            if (isMouseDown) {
                              toggleSelect(seatIdx, true);
                            }
                          }}
                          className={`h-9 w-9 rounded-lg border flex items-center justify-center text-[10px] font-bold cursor-pointer transition-all hover:scale-105 ${seatClass} ${
                            isSelected ? 'ring-2 ring-white border-white shadow-[0_0_15px_rgba(255,255,255,0.4)] z-10' : ''
                          }`}
                        >
                          {type === 'EMPTY' ? '' : `${rowChar}${colNum}`}
                        </div>
                      );
                    })}
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </main>

      {/* Cột Toolbar bên phải */}
      <aside className="w-80 bg-[#0F1320] border-l border-[#252A3D] p-6 flex flex-col justify-between shrink-0 h-full z-10">
        <div className="space-y-6">
          <label className="block text-[10px] font-bold tracking-wider text-[#7B82A0] uppercase">Công cụ / Loại ghế áp dụng</label>
          
          <div className="space-y-3">
            <div
              onClick={() => applyType('STANDARD')}
              className="flex items-center gap-3 p-4 rounded-xl border border-[#252A3D] bg-[#161B2E] cursor-pointer hover:bg-white/5 active:scale-[0.98] transition-all"
            >
              <div className="w-5 h-5 rounded bg-[#161B2E] border border-[#3A4060]" />
              <span className="text-sm font-semibold text-[#E8EAF0]">Ghế Thường (Standard)</span>
            </div>

            <div
              onClick={() => applyType('VIP')}
              className="flex items-center gap-3 p-4 rounded-xl border border-[#F5A623]/30 bg-[#161B2E] cursor-pointer hover:bg-white/5 active:scale-[0.98] transition-all"
            >
              <div className="w-5 h-5 rounded bg-[#F5A623]/20 border border-[#F5A623]" />
              <span className="text-sm font-semibold text-[#F5A623]">Ghế VIP (Gold)</span>
            </div>

            <div
              onClick={() => applyType('COUPLE')}
              className="flex items-center gap-3 p-4 rounded-xl border border-[#9B59B6]/30 bg-[#161B2E] cursor-pointer hover:bg-white/5 active:scale-[0.98] transition-all"
            >
              <div className="w-8 h-4 rounded bg-[#9B59B6]/20 border border-[#9B59B6]" />
              <span className="text-sm font-semibold text-[#9B59B6]">Ghế Đôi (Couple)</span>
            </div>

            <div
              onClick={() => applyType('EMPTY')}
              className="flex items-center gap-3 p-4 rounded-xl border border-[#252A3D] bg-transparent border-dashed cursor-pointer hover:bg-white/5 active:scale-[0.98] transition-all"
            >
              <div className="w-5 h-5 rounded border border-dashed border-[#6B7280]" />
              <span className="text-sm font-semibold text-[#7B82A0]">Ô Trống (Lối đi)</span>
            </div>
          </div>
        </div>

        {/* Action Save button */}
        <button
          onClick={saveLayout}
          className="w-full py-4 rounded-2xl bg-gradient-to-r from-[#E5133A] to-[#8B0020] hover:opacity-90 text-white font-bold tracking-wide active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-lg shadow-[#E5133A]/25"
        >
          <Save className="w-5 h-5" />
          LƯU SƠ ĐỒ PHÒNG
        </button>
      </aside>
    </div>
  );
}
