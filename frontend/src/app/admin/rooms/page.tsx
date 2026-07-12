'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Armchair, Plus, Search, Trash, Edit2, Wrench, RefreshCw, Grid, Layers, X, Save, Eye, Layout } from 'lucide-react';
import { useAuthStore } from '../../../store/authStore';
import { adminRoomService, adminBranchService, RoomData, BranchData } from '../../../services/admin';
import { hasPermission } from '../../../utils/rbac';
import api from '../../../config/api';

export default function AdminRoomsPage() {
  const { user } = useAuthStore();
  const isAdmin = hasPermission(user?.role, 'MANAGE_USERS'); // Admin toàn quyền
  const isManagerOrStaff = !isAdmin && hasPermission(user?.role, 'ACCESS_POS');

  const [rooms, setRooms] = useState<any[]>([]);
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [selectedBranchId, setSelectedBranchId] = useState<number | ''>('');
  const [roomTypes, setRoomTypes] = useState<any[]>([]);
  const [roomTemplates, setRoomTemplates] = useState<string[]>([]);
  
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Bộ lọc
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Trạng thái modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  
  // Form fields
  const [name, setName] = useState('');
  const [roomTypeId, setRoomTypeId] = useState('');
  const [status, setStatus] = useState<'ACTIVE' | 'MAINTENANCE' | 'INACTIVE'>('ACTIVE');
  const [templateFileName, setTemplateFileName] = useState('');
  const [rows, setRows] = useState<number>(10);
  const [cols, setCols] = useState<number>(10);
  const [submitting, setSubmitting] = useState(false);

  // Tải danh sách chi nhánh
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
        }
      }
    } catch (err) {
      console.error('Không thể lấy danh sách chi nhánh:', err);
    }
  };

  // Tải danh sách phòng chiếu
  const fetchRooms = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminRoomService.getRooms(selectedBranchId !== '' ? Number(selectedBranchId) : undefined);
      if (res?.success) {
        setRooms(res.data || []);
      } else {
        setErrorMsg(res?.message || 'Không thể tải danh sách phòng chiếu!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  // Tải metadata loại phòng và templates sơ đồ ghế
  const fetchMetadata = async () => {
    try {
      const [typeRes, tempRes] = await Promise.all([
        adminRoomService.getRoomTypes(),
        adminRoomService.getRoomTemplates(),
      ]);
      if (typeRes?.success) {
        setRoomTypes(typeRes.data || []);
        if (typeRes.data.length > 0) {
          setRoomTypeId(typeRes.data[0].id);
        }
      }
      if (tempRes?.success) {
        setRoomTemplates(tempRes.data || []);
      }
    } catch (err) {
      console.error('Lỗi tải dữ liệu metadata:', err);
    }
  };

  useEffect(() => {
    fetchBranches();
    fetchMetadata();
  }, [user]);

  useEffect(() => {
    fetchRooms();
  }, [selectedBranchId]);

  // Bộ lọc client-side
  const filteredRooms = rooms.filter((r) => {
    const matchesSearch = r.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = !typeFilter || r.roomTypeId === typeFilter;
    const matchesStatus = !statusFilter || r.status === statusFilter;
    return matchesSearch && matchesType && matchesStatus;
  });

  const handleOpenCreateModal = () => {
    setEditId(null);
    setName('');
    if (roomTypes.length > 0) setRoomTypeId(roomTypes[0].id);
    setStatus('ACTIVE');
    setTemplateFileName('');
    setRows(10);
    setCols(10);
    setShowModal(true);
  };

  const handleOpenEditModal = (room: any) => {
    setEditId(room.id);
    setName(room.name);
    setRoomTypeId(room.roomTypeId);
    setStatus(room.status || 'ACTIVE');
    setTemplateFileName('');
    setRows(room.rows || 10);
    setCols(room.cols || 10);
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const branchIdVal = selectedBranchId !== '' ? Number(selectedBranchId) : null;
    const data: RoomData = {
      name,
      roomTypeId,
      status,
      branchId: branchIdVal,
    };

    if (!templateFileName) {
      data.rows = rows;
      data.cols = cols;
    } else {
      data.templateFileName = templateFileName;
    }

    try {
      const res = editId !== null 
        ? await adminRoomService.updateRoom(editId, data)
        : await adminRoomService.createRoom(data);

      if (res?.success) {
        setShowModal(false);
        fetchRooms();
      } else {
        alert(res?.message || 'Lưu thông tin thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu phòng chiếu.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Xóa phòng này sẽ xóa toàn bộ suất chiếu và vé liên quan. Bạn chắc chắn muốn xóa?')) return;
    try {
      const res = await adminRoomService.deleteRoom(id);
      if (res?.success) {
        fetchRooms();
      } else {
        alert(res?.message || 'Xóa thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa phòng.');
    }
  };

  // Render sơ đồ ghế thu nhỏ (mini-layout)
  const renderMiniMap = (room: any) => {
    const seats = room.seats || [];
    const roomRows = room.rows || 1;
    const roomCols = room.cols || 1;

    // Sắp xếp ghế theo dòng và cột
    const sortedSeats = [...seats].sort((a, b) => {
      if (a.rowChar !== b.rowChar) return a.rowChar.localeCompare(b.rowChar);
      return a.colNum - b.colNum;
    });

    return (
      <div 
        className="grid gap-[2px] p-2 bg-black/10 rounded-xl justify-center items-center overflow-hidden" 
        style={{ gridTemplateColumns: `repeat(${roomCols}, minmax(0, 1fr))`, maxHeight: '110px' }}
      >
        {sortedSeats.map((s: any, idx: number) => {
          let seatColor = 'bg-[#3A4060]'; // standard
          if (s.seatTypeId === 'VIP') seatColor = 'bg-[#F5A623]';
          if (s.seatTypeId === 'COUPLE') seatColor = 'bg-[#9B59B6]';
          if (s.seatTypeId === 'EMPTY' || !s.status) seatColor = 'bg-transparent';

          return (
            <div 
              key={idx} 
              className={`w-1.5 h-1.5 rounded-[1px] ${seatColor}`} 
              title={`${s.rowChar}${s.colNum}`}
            />
          );
        })}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Armchair className="w-6 h-6 text-[#F5A623]" />
            Quản lý Phòng Chiếu
          </h1>
          <p className="text-sm text-[#6B7280]">
            Cấu hình không gian phòng chiếu, công nghệ và thiết kế sơ đồ ghế ngồi
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Dropdown chọn chi nhánh */}
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold text-[#6B7280]">Chi nhánh:</span>
            <select
              value={selectedBranchId}
              onChange={(e) => setSelectedBranchId(e.target.value ? Number(e.target.value) : '')}
              disabled={isManagerOrStaff}
              className="px-3 py-2 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] disabled:opacity-75 font-semibold"
            >
              {isAdmin && <option value="">Tất cả chi nhánh</option>}
              {branches.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={fetchRooms}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          {isAdmin && (
            <button
              onClick={handleOpenCreateModal}
              className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
            >
              <Plus className="w-5 h-5" />
              Thêm Phòng
            </button>
          )}
        </div>
      </div>

      {/* Filter Bar */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="relative">
          <Search className="w-4.5 h-4.5 absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
          <input
            type="text"
            placeholder="Tìm tên phòng chiếu..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] placeholder:text-[#6B7280]/60"
          />
        </div>

        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
        >
          <option value="">Tất cả loại phòng</option>
          {roomTypes.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE">Hoạt động</option>
          <option value="MAINTENANCE">Bảo trì</option>
          <option value="INACTIVE">Ngừng hoạt động</option>
        </select>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Grid danh sách phòng chiếu */}
      {loading ? (
        <div className="bg-white border border-black/5 rounded-2xl p-12 text-center text-[#6B7280] font-medium shadow-sm">
          Đang tải danh sách phòng chiếu...
        </div>
      ) : filteredRooms.length === 0 ? (
        <div className="bg-white border border-black/5 rounded-2xl p-12 text-center text-[#6B7280] font-medium shadow-sm">
          Không tìm thấy phòng chiếu nào.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredRooms.map((room) => {
            const statusLabels = {
              ACTIVE: { text: 'Hoạt động', cls: 'bg-green-50 text-green-700 border-green-200' },
              MAINTENANCE: { text: 'Bảo trì', cls: 'bg-amber-50 text-amber-700 border-amber-200' },
              INACTIVE: { text: 'Tạm ngưng', cls: 'bg-red-50 text-red-700 border-red-200' },
            };
            const currentStatus = statusLabels[room.status as 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE'] || { text: room.status, cls: 'bg-gray-50 text-gray-700 border-gray-200' };

            const typeLabel = (room.roomTypeId || '2D').replace('HALL_', '');

            return (
              <div
                key={room.id}
                className="bg-white border border-black/5 rounded-2xl p-5 flex flex-col justify-between transition-all hover:border-[#F5A623] hover:shadow-md relative overflow-hidden group shadow-sm"
              >
                {/* Type Badge */}
                <div className="absolute top-4 right-4">
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-lg text-[10px] font-extrabold bg-[#F5A623]/10 text-[#F5A623] uppercase tracking-wider">
                    {typeLabel}
                  </span>
                </div>

                <div>
                  {/* Status & Showtime Badges */}
                  <div className="flex flex-wrap items-center gap-1.5 mb-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-wider border ${currentStatus.cls}`}>
                      {currentStatus.text}
                    </span>
                    {room.hasShowtime && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-wider bg-blue-50 text-blue-700 border border-blue-200">
                        Có suất chiếu
                      </span>
                    )}
                  </div>

                  {/* Room Name */}
                  <h3 className="font-bold text-lg text-[#22232B] line-clamp-1 mb-1.5">{room.name}</h3>

                  {/* Formats chips */}
                  <div className="flex flex-wrap gap-1 mb-4">
                    {(room.supportedFormats || []).map((fmt: string, idx: number) => (
                      <span key={idx} className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-black/5 text-[#6B7280]">
                        {fmt}
                      </span>
                    ))}
                  </div>

                  {/* Stats info */}
                  <div className="flex items-center gap-4 text-xs font-semibold text-[#6B7280] mb-4">
                    <span className="flex items-center gap-1">
                      <Armchair className="w-3.5 h-3.5 text-[#F5A623]" />
                      {room.capacity || 0} Ghế
                    </span>
                    <span className="flex items-center gap-1">
                      <Grid className="w-3.5 h-3.5 text-[#6B7280]" />
                      {room.rows}x{room.cols}
                    </span>
                  </div>

                  {/* Mini Preview map */}
                  <div className="h-32 bg-[#FAFAFA] rounded-xl flex items-center justify-center p-3 mb-5 border border-black/5 overflow-hidden">
                    {renderMiniMap(room)}
                  </div>
                </div>

                {/* Actions row */}
                <div className="flex items-center gap-2 border-t border-black/5 pt-4 mt-auto">
                  {isAdmin ? (
                    room.hasShowtime ? (
                      <button
                        onClick={() => alert('Không thể thiết kế sơ đồ ghế của phòng chiếu đã lên lịch chiếu!')}
                        className="flex-1 py-2 px-3 border border-black/10 text-xs font-bold text-[#6B7280]/60 rounded-xl bg-black/5 cursor-not-allowed flex items-center justify-center gap-1.5"
                      >
                        <Layout className="w-3.5 h-3.5" />
                        Thiết kế
                      </button>
                    ) : (
                      <Link
                        href={`/admin/rooms/design/${room.id}`}
                        className="flex-1 py-2 px-3 border border-black/10 text-xs font-bold text-[#22232B] rounded-xl hover:border-[#F5A623] hover:text-[#F5A623] transition-colors flex items-center justify-center gap-1.5"
                      >
                        <Layout className="w-3.5 h-3.5" />
                        Thiết kế
                      </Link>
                    )
                  ) : null}

                  <button
                    onClick={() => handleOpenEditModal(room)}
                    className="p-2 border border-black/10 text-[#6B7280] hover:text-[#22232B] hover:bg-black/5 rounded-xl transition-all"
                    title="Chỉnh sửa thông tin"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>

                  {isAdmin && (
                    <button
                      onClick={() => handleDelete(room.id)}
                      className="p-2 border border-black/10 text-[#EF4444] hover:bg-red-50 hover:border-red-200 rounded-xl transition-all"
                      title="Xóa phòng"
                    >
                      <Trash className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Thêm/Sửa Phòng chiếu */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {editId !== null ? 'Sửa thông tin phòng chiếu' : 'Thêm phòng chiếu mới'}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280] transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Modal Form body */}
            <form onSubmit={handleSubmit}>
              <div className="p-6 space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên phòng chiếu</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="VD: Phòng chiếu 01 - IMAX"
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Loại phòng</label>
                    <select
                      value={roomTypeId}
                      onChange={(e) => setRoomTypeId(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                    >
                      {roomTypes.map((t) => (
                        <option key={t.id} value={t.id}>
                          {t.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Trạng thái</label>
                    <select
                      value={status}
                      onChange={(e) => setStatus(e.target.value as any)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                    >
                      <option value="ACTIVE">Hoạt động</option>
                      <option value="MAINTENANCE">Bảo trì</option>
                      <option value="INACTIVE">Ngừng hoạt động</option>
                    </select>
                  </div>
                </div>

                {editId === null && (
                  <>
                    <div>
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Template sơ đồ (Tùy chọn)</label>
                      <select
                        value={templateFileName}
                        onChange={(e) => setTemplateFileName(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                      >
                        <option value="">-- Tạo trống (Tự thiết lập dòng/cột) --</option>
                        {roomTemplates.map((name) => (
                          <option key={name} value={name}>
                            {name.replace('.json', '').replace(/_/g, ' ')}
                          </option>
                        ))}
                      </select>
                    </div>

                    {!templateFileName && (
                      <div className="grid grid-cols-2 gap-4 animate-in fade-in duration-200">
                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Số hàng ghế</label>
                          <input
                            type="number"
                            min="1"
                            max="26"
                            value={rows}
                            onChange={(e) => setRows(Number(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Số cột ghế</label>
                          <input
                            type="number"
                            min="1"
                            max="30"
                            value={cols}
                            onChange={(e) => setCols(Number(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                          />
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>

              {/* Modal Footer */}
              <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-5 py-2.5 border border-black/10 rounded-xl font-semibold text-[#6B7280] hover:bg-black/5 transition-all text-sm"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98] disabled:opacity-50 text-sm"
                >
                  <Save className="w-4 h-4" />
                  {submitting ? 'Đang lưu...' : 'Lưu lại'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
