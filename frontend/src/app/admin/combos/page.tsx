'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash, X, Save, Coffee, RefreshCw, Layers, Edit3, Check, Eye, EyeOff } from 'lucide-react';
import { useAuthStore } from '../../../store/authStore';
import { adminComboService, adminBranchService, ComboData, BranchData } from '../../../services/admin';
import { formatPrice } from '../../../utils/format';
import api from '../../../config/api';
import { hasPermission } from '../../../utils/rbac';

export default function AdminCombosPage() {
  const { user } = useAuthStore();
  const isAdmin = hasPermission(user?.role, 'MANAGE_USERS');
  const isManagerOrStaff = !isAdmin && hasPermission(user?.role, 'ACCESS_POS');

  const [mounted, setMounted] = useState(false);
  const [combos, setCombos] = useState<any[]>([]);
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [selectedBranchId, setSelectedBranchId] = useState<number | ''>('');
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Trạng thái modal Thêm/Sửa Combo
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [name, setName] = useState('');
  const [price, setPrice] = useState<number>(0);
  const [description, setDescription] = useState('');
  const [imageFile, setImageFile] = useState<File | undefined>(undefined);
  const [imagePreview, setImagePreview] = useState<string>('');
  const [isActive, setIsActive] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Trạng thái cập nhật tồn kho nhanh
  const [editingStockId, setEditingStockId] = useState<number | null>(null);
  const [newStockVal, setNewStockVal] = useState<number>(0);

  // Tải danh sách chi nhánh (Dành cho Admin để filter hoặc Staff để định vị chi nhánh)
  const fetchBranches = async () => {
    try {
      const res = await adminBranchService.getBranches();
      if (res?.success) {
        setBranches(res.data);
        // Nếu là manager/staff, cố gắng xác định chi nhánh của họ
        if (isManagerOrStaff) {
          try {
            const staffRes = await api.get('/staff/me');
            if (staffRes.data?.success && staffRes.data?.data?.branch?.id) {
              setSelectedBranchId(staffRes.data.data.branch.id);
            } else {
              // Fallback lấy chi nhánh đầu tiên nếu không fetch được
              if (res.data.length > 0) setSelectedBranchId(res.data[0].id);
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

  const fetchCombos = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      let res;
      if (typeof selectedBranchId === 'number') {
        res = await adminComboService.getCombosByBranch(Number(selectedBranchId));
      } else {
        res = await adminComboService.getCombos();
      }

      if (res?.success) {
        setCombos(Array.isArray(res.data) ? res.data : []);
      } else {
        setErrorMsg(res?.message || 'Không thể tải danh sách combo!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (mounted) fetchBranches();
  }, [user, mounted]);

  useEffect(() => {
    if (mounted) fetchCombos();
  }, [selectedBranchId, mounted]);

  const handleOpenCreateModal = () => {
    setEditId(null);
    setName('');
    setPrice(0);
    setDescription('');
    setImageFile(undefined);
    setImagePreview('');
    setIsActive(true);
    setShowModal(true);
  };

  const handleOpenEditModal = (combo: any) => {
    setEditId(combo.id);
    setName(combo.name);
    setPrice(combo.price);
    setDescription(combo.description || '');
    setImageFile(undefined);
    setImagePreview(combo.imageUrl || '');
    setIsActive(combo.isActive !== false);
    setShowModal(true);
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImageFile(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const data: ComboData = { name, price, description, isActive };

    try {
      let res;
      if (editId !== null) {
        res = await adminComboService.updateCombo(editId, data, imageFile);
      } else {
        res = await adminComboService.createCombo(data, imageFile);
      }

      if (res?.success) {
        setShowModal(false);
        fetchCombos();
      } else {
        alert(res?.message || 'Lưu thông tin thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu combo.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa combo này khỏi hệ thống?')) return;
    try {
      const res = await adminComboService.deleteCombo(id);
      if (res?.success) {
        fetchCombos();
      } else {
        alert(res?.message || 'Xóa thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa combo.');
    }
  };

  // --- Logic dành cho Rạp (Branch Manager / Staff) ---
  const handleToggleActiveBranch = async (comboId: number, currentActive: boolean) => {
    if (!selectedBranchId) return;
    try {
      const res = await adminComboService.toggleBranchActive(Number(selectedBranchId), comboId, !currentActive);
      if (res?.success) {
        fetchCombos();
      } else {
        alert(res?.message || 'Không thể thay đổi trạng thái bán!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    }
  };

  const handleShowEditStock = (combo: any) => {
    setEditingStockId(combo.id);
    setNewStockVal(combo.stockQuantity || 0);
  };

  const handleSaveStock = async (comboId: number) => {
    if (!selectedBranchId) return;
    try {
      const res = await adminComboService.updateBranchStock(Number(selectedBranchId), comboId, newStockVal);
      if (res?.success) {
        setEditingStockId(null);
        fetchCombos();
      } else {
        alert(res?.message || 'Cập nhật tồn kho thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    }
  };

  if (!mounted) {
    return (
      <div className="bg-[#FAFAFA] p-8 text-center text-[#6B7280] font-medium">
        Đang tải...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Coffee className="w-6 h-6 text-[#F5A623]" />
            Quản lý Combos Bắp Nước
          </h1>
          <p className="text-sm text-[#6B7280]">
            {isAdmin
              ? 'Định nghĩa danh mục combo bắp nước và thiết lập giá bán toàn hệ thống'
              : 'Cập nhật số lượng tồn kho và cấu hình trạng thái kinh doanh tại chi nhánh'}
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
              {isAdmin && <option value="">Tất cả hệ thống</option>}
              {branches.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={fetchCombos}
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
              Thêm Combo
            </button>
          )}
        </div>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Grid danh sách combo */}
      {loading ? (
        <div className="bg-white border border-black/5 rounded-2xl p-12 text-center text-[#6B7280] font-medium shadow-sm">
          Đang tải danh sách combo bắp nước...
        </div>
      ) : combos.length === 0 ? (
        <div className="bg-white border border-black/5 rounded-2xl p-12 text-center text-[#6B7280] font-medium shadow-sm">
          Chưa có combo nào.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {combos.map((combo) => {
            const isComboActive = combo.isActive !== false;
            return (
              <div
                key={combo.id}
                className={`bg-white border rounded-2xl p-4 flex flex-col justify-between transition-all relative overflow-hidden group shadow-sm ${
                  isComboActive
                    ? 'border-black/5 hover:border-[#F5A623] hover:shadow-md'
                    : 'border-dashed border-gray-300 opacity-80 filter grayscale-[20%]'
                }`}
              >
                {/* Admin Quick Edit buttons */}
                {isAdmin && (
                  <div className="absolute top-3 left-3 flex items-center gap-1.5 opacity-0 group-hover:opacity-100 transition-opacity z-10">
                    <button
                      onClick={() => handleOpenEditModal(combo)}
                      className="w-8 h-8 rounded-lg bg-blue-500 text-white flex items-center justify-center hover:bg-blue-600 shadow-md transition-colors"
                      title="Chỉnh sửa thông tin gốc"
                    >
                      <Pencil className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(combo.id)}
                      className="w-8 h-8 rounded-lg bg-red-500 text-white flex items-center justify-center hover:bg-red-600 shadow-md transition-colors"
                      title="Xóa khỏi hệ thống"
                    >
                      <Trash className="w-3.5 h-3.5" />
                    </button>
                  </div>
                )}

                {/* Badge trạng thái bán */}
                <div className="absolute top-3 right-3 z-10">
                  <span
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                      isComboActive
                        ? 'bg-green-50 text-green-700 border border-green-200'
                        : 'bg-red-50 text-red-600 border border-red-200'
                    }`}
                  >
                    {isComboActive ? 'Đang bán' : 'Tạm ngưng'}
                  </span>
                </div>

                <div>
                  {/* Ảnh combo */}
                  <div className="w-full h-40 bg-[#FAFAFA] rounded-xl overflow-hidden mb-3 border border-black/5">
                    <img
                      src={combo.imageUrl || 'https://placehold.co/300x180/FAFAFA/6B7280?text=Combo'}
                      alt={combo.name}
                      className="w-full h-full object-cover"
                      onError={(e: any) => {
                        e.target.src = 'https://placehold.co/300x180/FAFAFA/6B7280?text=Combo';
                      }}
                    />
                  </div>

                  {/* Tên combo */}
                  <h3 className="font-bold text-base text-[#22232B] line-clamp-1 mb-1">{combo.name}</h3>
                  {/* Giá bán */}
                  <div className="text-sm font-bold text-[#F5A623] mb-3">{formatPrice(combo.price)}</div>

                  {/* Mô tả (Dành cho Admin) */}
                  {isAdmin && (
                    <p className="text-xs text-[#6B7280] line-clamp-2 min-h-[2rem] border-t border-black/5 pt-2">
                      {combo.description || 'Không có mô tả chi tiết.'}
                    </p>
                  )}
                </div>

                {/* Tồn kho và Trạng thái tại Chi Nhánh (Dành cho Manager/Staff hoặc Admin khi chọn cụ thể chi nhánh) */}
                {typeof selectedBranchId === 'number' && (
                  <div className="mt-4 pt-3 border-t border-black/5 bg-[#FAFAFA] -mx-4 -mb-4 p-4 rounded-b-2xl">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-[10px] font-bold uppercase tracking-wider text-[#6B7280]">Kinh doanh tại rạp</span>
                      <button
                        onClick={() => handleToggleActiveBranch(combo.id, isComboActive)}
                        className={`p-1 rounded-lg border transition-all ${
                          isComboActive
                            ? 'text-green-600 bg-green-50 border-green-200 hover:bg-green-100'
                            : 'text-gray-400 bg-white border-gray-200 hover:bg-gray-50'
                        }`}
                        title={isComboActive ? 'Tạm ngưng bán combo tại rạp này' : 'Mở bán combo tại rạp này'}
                      >
                        {isComboActive ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                      </button>
                    </div>

                    {/* Hiển thị tồn kho */}
                    {editingStockId === combo.id ? (
                      <div className="space-y-1.5 animate-in fade-in duration-200">
                        <span className="text-[10px] font-bold uppercase tracking-wider text-[#6B7280]">Số lượng tồn mới</span>
                        <div className="flex items-center gap-2">
                          <input
                            type="number"
                            min="0"
                            value={newStockVal}
                            onChange={(e) => setNewStockVal(Number(e.target.value))}
                            className="w-full px-2 py-1 text-sm border border-black/10 rounded-lg text-[#22232B] font-bold text-center bg-white"
                          />
                          <button
                            onClick={() => handleSaveStock(combo.id)}
                            className="p-1.5 bg-[#F5A623] text-white rounded-lg hover:bg-[#E09415]"
                          >
                            <Check className="w-4 h-4" />
                          </button>
                        </div>
                        <button
                          onClick={() => setEditingStockId(null)}
                          className="text-[10px] text-[#6B7280] hover:text-[#22232B] block w-full text-center hover:underline"
                        >
                          Hủy bỏ
                        </button>
                      </div>
                    ) : (
                      <div className="flex items-center justify-between">
                        <div>
                          <span className="text-[10px] font-bold uppercase tracking-wider text-[#6B7280] block">Tồn kho chi nhánh</span>
                          <div className="text-base font-bold text-[#22232B]">
                            {combo.stockQuantity || 0} <span className="text-xs text-[#6B7280] font-normal">PHẦN</span>
                          </div>
                        </div>
                        <button
                          onClick={() => handleShowEditStock(combo)}
                          className="p-1.5 text-[#6B7280] hover:text-[#F5A623] hover:bg-black/5 rounded-lg transition-all"
                          title="Cập nhật tồn kho"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Thêm/Sửa Combo (Chỉ dành cho Admin) */}
      {showModal && isAdmin && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {editId !== null ? 'Sửa thông tin Combo' : 'Thêm Combo mới'}
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
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên Combo</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="VD: Combo Single Size L"
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giá bán (VNĐ)</label>
                  <input
                    type="number"
                    required
                    min="0"
                    value={price}
                    onChange={(e) => setPrice(Number(e.target.value))}
                    placeholder="VD: 100000"
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Mô tả chi tiết</label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="VD: Gồm 1 Bắp L ngọt và 1 Pepsi L..."
                    rows={3}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Hình ảnh Combo</label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="w-full text-xs text-[#6B7280] file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-[#F5A623]/10 file:text-[#F5A623] hover:file:bg-[#F5A623]/20 cursor-pointer"
                  />
                  {imagePreview && (
                    <div className="mt-3 w-full h-32 rounded-xl overflow-hidden border border-black/5 relative">
                      <img src={imagePreview} className="w-full h-full object-cover" alt="Preview" />
                      <button
                        type="button"
                        onClick={() => {
                          setImageFile(undefined);
                          setImagePreview('');
                        }}
                        className="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/60 text-white flex items-center justify-center hover:bg-black/80"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-3 pt-2">
                  <input
                    type="checkbox"
                    id="isActive"
                    checked={isActive}
                    onChange={(e) => setIsActive(e.target.checked)}
                    className="w-4.5 h-4.5 text-[#F5A623] border-black/10 rounded focus:ring-[#F5A623] accent-[#F5A623]"
                  />
                  <label htmlFor="isActive" className="text-sm font-semibold text-[#22232B] select-none cursor-pointer">
                    Combo đang bán trên hệ thống
                  </label>
                </div>
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
                  {submitting ? 'Đang tải lên...' : 'Lưu lại'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
