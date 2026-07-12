'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash, X, Save, MapPin, Phone, Building } from 'lucide-react';
import { adminBranchService, BranchData } from '../../../services/admin';

export default function AdminBranchesPage() {
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Trạng thái modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);

  // Dữ liệu form
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [phone, setPhone] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const fetchBranches = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminBranchService.getBranches();
      if (res?.success) {
        setBranches(res.data);
      } else {
        setErrorMsg(res?.message || 'Không thể tải danh sách chi nhánh!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBranches();
  }, []);

  const handleOpenCreateModal = () => {
    setEditId(null);
    setName('');
    setAddress('');
    setCity('');
    setPhone('');
    setIsActive(true);
    setShowModal(true);
  };

  const handleOpenEditModal = (branch: BranchData) => {
    if (branch.id === undefined) return;
    setEditId(branch.id);
    setName(branch.name);
    setAddress(branch.address);
    setCity(branch.city);
    setPhone(branch.phone);
    setIsActive(branch.isActive);
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const data: BranchData = { name, address, city, phone, isActive };

    try {
      let res;
      if (editId !== null) {
        res = await adminBranchService.updateBranch(editId, data);
      } else {
        res = await adminBranchService.createBranch(data);
      }

      if (res?.success) {
        setShowModal(false);
        fetchBranches();
      } else {
        alert(res?.message || 'Thao tác thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu chi nhánh.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa chi nhánh này? Mọi phòng chiếu và dữ liệu liên quan sẽ bị ảnh hưởng!')) return;
    try {
      const res = await adminBranchService.deleteBranch(id);
      if (res?.success) {
        fetchBranches();
      } else {
        alert(res?.message || 'Xóa thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa chi nhánh.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Building className="w-6 h-6 text-[#F5A623]" />
            Quản lý Chi Nhánh
          </h1>
          <p className="text-sm text-[#6B7280]">Thiết lập và vận hành các rạp chiếu phim trong chuỗi StarCinema</p>
        </div>
        <button
          onClick={handleOpenCreateModal}
          className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
        >
          <Plus className="w-5 h-5" />
          Thêm chi nhánh
        </button>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Main content table */}
      <div className="bg-white border border-black/5 rounded-2xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Đang tải danh sách chi nhánh...</div>
        ) : branches.length === 0 ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Chưa có chi nhánh nào được cấu hình.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[#FAFAFA] border-b border-black/5">
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Tên chi nhánh</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Địa chỉ / Thành phố</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Số điện thoại</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider text-right">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5">
                {branches.map((branch) => (
                  <tr key={branch.id} className="hover:bg-black/[0.01] transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-bold text-[#22232B]">{branch.name}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-[#22232B]">
                      <div className="flex items-center gap-1.5">
                        <MapPin className="w-4 h-4 text-[#6B7280]" />
                        {branch.address}, {branch.city}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-[#22232B]">
                      <div className="flex items-center gap-1.5">
                        <Phone className="w-4 h-4 text-[#6B7280]" />
                        {branch.phone}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${
                          branch.isActive
                            ? 'bg-green-50 text-green-700 border border-green-200'
                            : 'bg-gray-100 text-gray-600 border border-gray-200'
                        }`}
                      >
                        {branch.isActive ? 'Hoạt động' : 'Tạm dừng'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleOpenEditModal(branch)}
                          className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-[#F5A623] hover:border-[#F5A623] transition-all bg-white"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => branch.id && handleDelete(branch.id)}
                          className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#EF4444] hover:bg-red-500/5 hover:border-red-200 transition-all bg-white"
                        >
                          <Trash className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Slideover / Popup */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {editId !== null ? 'Chỉnh sửa chi nhánh' : 'Thêm chi nhánh mới'}
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
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên chi nhánh</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="VD: StarCinema Thủ Đức"
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Địa chỉ</label>
                  <input
                    type="text"
                    required
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    placeholder="VD: 123 Đường số 4"
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Thành phố</label>
                    <input
                      type="text"
                      required
                      value={city}
                      onChange={(e) => setCity(e.target.value)}
                      placeholder="VD: Hồ Chí Minh"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Số điện thoại</label>
                    <input
                      type="text"
                      required
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="VD: 0281234567"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>
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
                    Chi nhánh đang hoạt động
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
