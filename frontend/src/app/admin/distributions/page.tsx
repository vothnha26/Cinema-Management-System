'use client';

import { useState, useEffect } from 'react';
import { Film, Building2, Search, Plus, Trash2, ArrowLeft, Settings, Award } from 'lucide-react';
import { 
  adminDistributionService, 
  adminBranchService, 
  adminMovieService,
  BranchMovieData, 
  BranchData,
  MovieRequestData
} from '../../../services/admin';

export default function AdminDistributionsPage() {
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [selectedBranchId, setSelectedBranchId] = useState<string>('');
  const [allMovies, setAllMovies] = useState<MovieRequestData[]>([]);
  const [branchMovies, setBranchMovies] = useState<BranchMovieData[]>([]);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [syncMessage, setSyncMessage] = useState('Đồng bộ dữ liệu');
  
  // States for Priority Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingBmId, setEditingBmId] = useState<number | null>(null);
  const [editingPriority, setEditingPriority] = useState<number>(5);

  // Initialize: Load branches and all movies
  useEffect(() => {
    const init = async () => {
      setLoading(true);
      setErrorMsg('');
      try {
        const bRes = await adminBranchService.getBranches();
        if (bRes?.success) {
          setBranches(bRes.data || []);
        }

        const mRes = await adminMovieService.getMovies();
        if (mRes?.success) {
          setAllMovies(mRes.data || []);
        }
      } catch (err: any) {
        console.error(err);
        setErrorMsg('Lỗi khi khởi tạo dữ liệu.');
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  // Load branch movies when branch selection changes
  const loadBranchMovies = async (branchId: number) => {
    setErrorMsg('');
    try {
      const res = await adminDistributionService.getBranchMovies(branchId);
      if (res?.success) {
        setBranchMovies(res.data || []);
      } else {
        setErrorMsg(res?.message || 'Không thể tải danh sách phim của chi nhánh.');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg('Lỗi tải danh sách phim của chi nhánh.');
    }
  };

  const handleBranchChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const val = e.target.value;
    setSelectedBranchId(val);
    if (val) {
      loadBranchMovies(Number(val));
    } else {
      setBranchMovies([]);
    }
  };

  // Add Movie to Branch
  const handleAddMovie = async (movieId: number) => {
    if (!selectedBranchId) {
      alert('Vui lòng chọn chi nhánh trước!');
      return;
    }
    setSubmitting(true);
    try {
      const res = await adminDistributionService.addMovieToBranch(Number(selectedBranchId), movieId, 5);
      if (res?.success) {
        setSyncMessage('Đang cập nhật...');
        await loadBranchMovies(Number(selectedBranchId));
        setSyncMessage('Đã đồng bộ');
        setTimeout(() => setSyncMessage('Đồng bộ dữ liệu'), 2000);
      } else {
        alert(res?.message || 'Không thể phân bổ phim.');
      }
    } catch (err: any) {
      console.error(err);
      alert('Lỗi phân bổ phim.');
    } finally {
      setSubmitting(false);
    }
  };

  // Remove Movie from Branch
  const handleRemoveMovie = async (bmId: number) => {
    if (!confirm('Xác nhận gỡ phim này khỏi chi nhánh?')) return;
    setSubmitting(true);
    try {
      const res = await adminDistributionService.removeMovieFromBranch(bmId);
      if (res?.success) {
        setSyncMessage('Đang gỡ phim...');
        await loadBranchMovies(Number(selectedBranchId));
        setSyncMessage('Đã gỡ');
        setTimeout(() => setSyncMessage('Đồng bộ dữ liệu'), 2000);
      } else {
        alert(res?.message || 'Không thể gỡ phim.');
      }
    } catch (err: any) {
      console.error(err);
      alert('Lỗi gỡ phim.');
    } finally {
      setSubmitting(false);
    }
  };

  // Open Priority Modal
  const openPriorityModal = (bmId: number, currentPriority: number) => {
    setEditingBmId(bmId);
    setEditingPriority(currentPriority);
    setIsModalOpen(true);
  };

  // Save Priority Level
  const savePriority = async () => {
    if (editingBmId === null) return;
    setSubmitting(true);
    try {
      const res = await adminDistributionService.updateMoviePriority(editingBmId, editingPriority);
      if (res?.success) {
        setIsModalOpen(false);
        setEditingBmId(null);
        await loadBranchMovies(Number(selectedBranchId));
      } else {
        alert(res?.message || 'Cập nhật độ ưu tiên thất bại.');
      }
    } catch (err: any) {
      console.error(err);
      alert('Lỗi cập nhật độ ưu tiên.');
    } finally {
      setSubmitting(false);
    }
  };

  // Filter out movies already distributed in the current branch
  const availableMovies = allMovies.filter(m => {
    const isDistributed = branchMovies.some(bm => bm.movie.id === m.id);
    const matchesSearch = m.title.toLowerCase().includes(searchQuery.toLowerCase());
    return !isDistributed && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Film className="w-6 h-6 text-[#E5133A]" />
            Phân bổ phim & Chi nhánh
          </h1>
          <p className="text-sm text-[#6B7280]">
            Phân phối danh mục phim khả dụng từ kho hệ thống xuống từng chi nhánh rạp chiếu
          </p>
        </div>

        <button 
          onClick={() => window.location.href = '/admin/movies'}
          className="flex items-center gap-2 px-4 py-2 border border-black/10 hover:bg-black/5 text-[#22232B] text-xs font-bold rounded-xl transition-all"
        >
          <ArrowLeft className="w-4 h-4" />
          KHO PHIM HỆ THỐNG
        </button>
      </div>

      {/* Select branch bar */}
      <div className="bg-white border border-black/5 rounded-2xl p-6 shadow-sm flex flex-col md:flex-row md:items-center gap-4">
        <div className="flex items-center gap-3 text-red-600">
          <Building2 className="w-6 h-6 text-[#E5133A]" />
        </div>
        <div className="flex-1">
          <label className="block text-[10px] font-bold text-[#6B7280] uppercase tracking-wider mb-1">
            Chọn chi nhánh cấu hình
          </label>
          <select
            value={selectedBranchId}
            onChange={handleBranchChange}
            className="w-full md:w-80 px-4 py-2.5 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm text-[#22232B] font-bold outline-none focus:border-[#E5133A]"
          >
            <option value="">-- Chọn chi nhánh --</option>
            {branches.map(b => (
              <option key={b.id} value={b.id}>
                {b.name} ({b.city})
              </option>
            ))}
          </select>
        </div>

        <div className="md:ml-auto text-right">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-green-50 border border-green-100 rounded-lg text-xs font-bold text-green-700">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
            {syncMessage}
          </div>
        </div>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Two columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left Column: Movie Library */}
        <div className="bg-white border border-black/5 rounded-2xl shadow-sm flex flex-col h-[650px] overflow-hidden">
          <div className="p-5 border-b border-black/5 flex items-center justify-between bg-[#FAFAFA]">
            <h6 className="text-sm font-bold text-[#22232B] uppercase tracking-wider">
              Kho Phim Hệ Thống
            </h6>
            <span className="px-2.5 py-0.5 bg-black/5 rounded-full text-xs font-bold text-[#6B7280]">
              {availableMovies.length} Phim
            </span>
          </div>

          <div className="p-4 border-b border-black/5">
            <div className="relative">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
              <input
                type="text"
                placeholder="Tìm phim trong kho..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-black/10 text-xs outline-none focus:border-[#E5133A] text-[#22232B] placeholder:text-[#6B7280]/60 font-semibold"
              />
            </div>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {loading ? (
              <div className="text-center py-12 text-sm text-[#6B7280] font-semibold">
                Đang tải kho phim...
              </div>
            ) : availableMovies.length === 0 ? (
              <div className="text-center py-12 text-sm text-[#6B7280] font-semibold">
                Không tìm thấy phim khả dụng nào.
              </div>
            ) : (
              availableMovies.map((m) => (
                <div key={m.id} className="p-3 bg-[#FAFAFA] border border-black/5 rounded-xl flex items-center gap-4 hover:border-black/20 transition-all">
                  <img
                    src={m.posterUrl || 'https://placehold.co/45x65'}
                    alt={m.title}
                    className="w-11 h-16 rounded-lg object-cover bg-black/5 shadow-sm"
                  />
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-bold text-[#22232B] truncate">{m.title}</h4>
                    <p className="text-xs text-[#6B7280] font-semibold mt-1">
                      {m.duration} phút • {m.ageRating}
                    </p>
                  </div>
                  <button
                    onClick={() => m.id !== undefined && handleAddMovie(m.id)}
                    disabled={submitting || !selectedBranchId}
                    className="p-2 bg-green-50 border border-green-200 text-green-700 rounded-xl hover:bg-green-100 transition-colors disabled:opacity-50"
                    title="Phân bổ xuống rạp"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right Column: Branch Distributed Movies */}
        <div className="bg-white border border-black/5 rounded-2xl shadow-sm flex flex-col h-[650px] overflow-hidden">
          <div className="p-5 border-b border-black/5 flex items-center justify-between bg-[#FAFAFA]">
            <h6 className="text-sm font-bold text-[#22232B] uppercase tracking-wider">
              Phim Đang Chiếu Tại Rạp
            </h6>
            <span className="px-2.5 py-0.5 bg-[#E5133A]/10 rounded-full text-xs font-bold text-[#E5133A]">
              {branchMovies.length} Phim
            </span>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {!selectedBranchId ? (
              <div className="text-center py-24 text-sm text-[#6B7280] font-semibold">
                Vui lòng chọn một chi nhánh để quản lý
              </div>
            ) : branchMovies.length === 0 ? (
              <div className="text-center py-24 text-sm text-[#6B7280] font-semibold">
                Chi nhánh này chưa được phân bổ phim nào.
              </div>
            ) : (
              branchMovies.map((bm) => (
                <div key={bm.id} className="p-3 bg-[#FAFAFA] border border-black/5 rounded-xl flex items-center gap-4 hover:border-black/20 transition-all">
                  <img
                    src={bm.movie.posterUrl || 'https://placehold.co/45x65'}
                    alt={bm.movie.title}
                    className="w-11 h-16 rounded-lg object-cover bg-black/5 shadow-sm"
                  />
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-bold text-[#22232B] truncate">{bm.movie.title}</h4>
                    <div className="flex items-center gap-3 mt-1.5">
                      <button
                        onClick={() => openPriorityModal(bm.id, bm.priority)}
                        className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-indigo-50 border border-indigo-100 hover:bg-indigo-100 rounded-lg text-xs font-extrabold text-indigo-700 cursor-pointer transition-colors"
                        title="Nhấp để thay đổi ưu tiên AI"
                      >
                        <Award className="w-3.5 h-3.5" />
                        P: {bm.priority}
                      </button>
                      <span className="text-[10px] text-[#6B7280] font-bold">
                        Phân bổ: {new Date(bm.assignedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                  </div>
                  <button
                    onClick={() => handleRemoveMovie(bm.id)}
                    disabled={submitting}
                    className="p-2 bg-red-50 border border-red-200 text-red-600 rounded-xl hover:bg-red-100 transition-colors disabled:opacity-50"
                    title="Gỡ khỏi rạp"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Priority Level Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white border border-black/5 rounded-2xl w-full max-w-sm overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="p-5 border-b border-black/5 bg-[#FAFAFA] flex items-center justify-between">
              <h3 className="font-bold text-[#22232B] text-sm uppercase tracking-wider flex items-center gap-2">
                <Settings className="w-4.5 h-4.5 text-indigo-600" />
                Mức Độ Ưu Tiên AI
              </h3>
              <button 
                onClick={() => setIsModalOpen(false)}
                className="text-xs font-semibold text-[#6B7280] hover:text-[#22232B]"
              >
                Đóng
              </button>
            </div>
            
            <div className="p-6 text-center space-y-4">
              <input
                type="number"
                min="1"
                max="10"
                value={editingPriority}
                onChange={(e) => setEditingPriority(Math.max(1, Math.min(10, Number(e.target.value))))}
                className="w-24 text-center text-3xl font-extrabold text-[#E5133A] bg-[#FAFAFA] border border-black/10 py-2 rounded-xl outline-none focus:border-[#E5133A]"
              />
              <p className="text-xs text-[#6B7280] font-semibold leading-relaxed">
                Mức độ ưu tiên nhận giá trị từ 1 đến 10. Trọng số này được hệ thống AI Scheduler sử dụng để ưu tiên phân phối suất chiếu vào khung giờ vàng (Prime Time).
              </p>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2.5 border border-black/10 text-xs font-bold text-[#22232B] rounded-xl hover:bg-black/5 transition-all"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={savePriority}
                  disabled={submitting}
                  className="flex-1 py-2.5 bg-gradient-to-r from-[#E5133A] to-[#8B0020] hover:opacity-95 text-white text-xs font-bold rounded-xl active:scale-[0.98] transition-all shadow-md shadow-[#E5133A]/10"
                >
                  {submitting ? 'Đang cập nhật...' : 'Cập nhật'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
