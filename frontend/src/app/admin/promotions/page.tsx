'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash, X, Save, BadgePercent, RefreshCw, Calendar, Tag, Gift, Award } from 'lucide-react';
import { adminPromotionService, PromotionData } from '../../../services/admin';
import { formatPrice } from '../../../utils/format';

export default function AdminPromotionsPage() {
  const [promotions, setPromotions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Trạng thái modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);

  // Dữ liệu form
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [discountType, setDiscountType] = useState<'PERCENT' | 'FLAT'>('PERCENT');
  const [discountValue, setDiscountValue] = useState<number>(0);
  const [minOrderAmount, setMinOrderAmount] = useState<number>(0);
  const [maxDiscountAmount, setMaxDiscountAmount] = useState<number>(0);
  const [minLevelName, setMinLevelName] = useState('');
  const [requiredPoints, setRequiredPoints] = useState<number>(0);
  const [usageLimit, setUsageLimit] = useState<number>(1000);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const fetchPromotions = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminPromotionService.getPromotions();
      if (res?.success) {
        setPromotions(Array.isArray(res.data) ? res.data : []);
      } else {
        setErrorMsg(res?.message || 'Không thể tải danh sách khuyến mãi!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPromotions();
  }, []);

  const handleOpenCreateModal = () => {
    setEditId(null);
    setCode('');
    setName('');
    setDiscountType('PERCENT');
    setDiscountValue(0);
    setMinOrderAmount(0);
    setMaxDiscountAmount(0);
    setMinLevelName('');
    setRequiredPoints(0);
    setUsageLimit(1000);
    setStartDate('');
    setEndDate('');
    setIsActive(true);
    setShowModal(true);
  };

  const handleOpenEditModal = (promo: any) => {
    setEditId(promo.id);
    setCode(promo.code);
    setName(promo.name);
    setDiscountType(promo.discountType);
    setDiscountValue(promo.discountValue);
    setMinOrderAmount(promo.minOrderAmount || 0);
    setMaxDiscountAmount(promo.maxDiscountAmount || 0);
    setMinLevelName(promo.minLevelName || '');
    setRequiredPoints(promo.requiredPoints || 0);
    setUsageLimit(promo.usageLimit || 1000);
    setStartDate(promo.startDate || '');
    setEndDate(promo.endDate || '');
    setIsActive(promo.isActive !== false);
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    const data: any = {
      code: code.trim().toUpperCase(),
      name: name.trim(),
      discountType,
      discountValue,
      minOrderAmount,
      maxDiscountAmount: discountType === 'PERCENT' ? maxDiscountAmount : 0, // Chỉ áp dụng trần cho %
      minLevelName: minLevelName || null,
      requiredPoints,
      isRedeemable: requiredPoints > 0,
      usageLimit,
      startDate,
      endDate,
      isActive,
    };

    try {
      let res;
      if (editId !== null) {
        res = await adminPromotionService.updatePromotion(editId, data);
      } else {
        res = await adminPromotionService.createPromotion(data);
      }

      if (res?.success) {
        setShowModal(false);
        fetchPromotions();
      } else {
        alert(res?.message || 'Thao tác thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu mã giảm giá.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa mã giảm giá này?')) return;
    try {
      const res = await adminPromotionService.deletePromotion(id);
      if (res?.success) {
        fetchPromotions();
      } else {
        alert(res?.message || 'Xóa thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa khuyến mãi.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <BadgePercent className="w-6 h-6 text-[#F5A623]" />
            Quản lý Khuyến Mãi & Vouchers
          </h1>
          <p className="text-sm text-[#6B7280]">Cấu hình các chương trình ưu đãi, mã giảm giá và quà tặng đổi điểm thành viên</p>
        </div>
        <button
          onClick={handleOpenCreateModal}
          className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
        >
          <Plus className="w-5 h-5" />
          Tạo Voucher mới
        </button>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Main content Table */}
      <div className="bg-white border border-black/5 rounded-2xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Đang tải danh sách khuyến mãi...</div>
        ) : promotions.length === 0 ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Chưa có chương trình khuyến mãi nào được tạo.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[#FAFAFA] border-b border-black/5">
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Mã Voucher</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Tên chương trình</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Loại giảm</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Hạng tối thiểu</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Đổi điểm</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5">
                {promotions.map((promo) => {
                  const isPercent = promo.discountType === 'PERCENT';
                  const discountStr = isPercent
                    ? `${promo.discountValue}%`
                    : formatPrice(promo.discountValue);

                  return (
                    <tr key={promo.id} className="hover:bg-black/[0.01] transition-colors">
                      <td className="px-6 py-4">
                        <span className="px-3 py-1.5 rounded-lg bg-red-50 border border-red-200 text-[#EF4444] font-bold font-mono text-sm tracking-wide">
                          {promo.code}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="font-bold text-[#22232B]">{promo.name}</div>
                        <div className="text-xs text-[#6B7280] flex items-center gap-1 mt-0.5">
                          <Calendar className="w-3.5 h-3.5" />
                          Hạn: {promo.startDate} đến {promo.endDate}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="font-bold text-[#F5A623]">{discountStr}</div>
                        {isPercent && promo.maxDiscountAmount > 0 && (
                          <div className="text-[10px] text-[#6B7280]">Tối đa {formatPrice(promo.maxDiscountAmount)}</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-gray-100 border border-gray-200 text-xs font-semibold text-[#22232B]">
                          <Award className="w-3.5 h-3.5 text-[#6B7280]" />
                          {promo.minLevelName || 'GUEST'}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center gap-1 text-sm font-semibold text-blue-600">
                          <Gift className="w-4 h-4" />
                          {promo.requiredPoints || 0} pts
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${
                            promo.isActive
                              ? 'bg-green-50 text-green-700 border border-green-200'
                              : 'bg-red-50 text-red-700 border border-red-200'
                          }`}
                        >
                          {promo.isActive ? 'Đang chạy' : 'Vô hiệu'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleOpenEditModal(promo)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-[#F5A623] hover:border-[#F5A623] transition-all bg-white"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(promo.id)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#EF4444] hover:bg-red-500/5 hover:border-red-200 transition-all bg-white"
                          >
                            <Trash className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Popup */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {editId !== null ? 'Cập nhật Voucher' : 'Tạo Voucher mới'}
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
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5 flex items-center gap-1">
                      <Tag className="w-4 h-4 text-[#F5A623]" /> Mã Voucher *
                    </label>
                    <input
                      type="text"
                      required
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      placeholder="VD: SUMMER26"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-mono font-bold"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên chương trình *</label>
                    <input
                      type="text"
                      required
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="VD: Khuyến mãi hè rực rỡ"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Loại giảm giá</label>
                    <select
                      value={discountType}
                      onChange={(e) => setDiscountType(e.target.value as any)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                    >
                      <option value="PERCENT">Giảm theo %</option>
                      <option value="FLAT">Giảm số tiền cố định</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giá trị giảm *</label>
                    <input
                      type="number"
                      required
                      min="1"
                      value={discountValue}
                      onChange={(e) => setDiscountValue(Number(e.target.value))}
                      placeholder={discountType === 'PERCENT' ? 'VD: 15 (%)' : 'VD: 50000 (VNĐ)'}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Đơn tối thiểu (VNĐ)</label>
                    <input
                      type="number"
                      min="0"
                      value={minOrderAmount}
                      onChange={(e) => setMinOrderAmount(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  {discountType === 'PERCENT' ? (
                    <div className="animate-in fade-in duration-200">
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giảm tối đa (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={maxDiscountAmount}
                        onChange={(e) => setMaxDiscountAmount(Number(e.target.value))}
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                      />
                    </div>
                  ) : (
                    <div className="opacity-40 pointer-events-none select-none">
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giảm tối đa (N/A)</label>
                      <input
                        type="number"
                        disabled
                        value={0}
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-[#FAFAFA] text-sm text-[#6B7280]"
                      />
                    </div>
                  )}

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Hạng thành viên tối thiểu</label>
                    <select
                      value={minLevelName}
                      onChange={(e) => setMinLevelName(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    >
                      <option value="">GUEST (Khách vãng lai)</option>
                      <option value="STANDARD">STANDARD</option>
                      <option value="SILVER">SILVER</option>
                      <option value="GOLD">GOLD</option>
                      <option value="PLATINUM">PLATINUM</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5 text-blue-600">Điểm đổi (0 nếu tặng free)</label>
                    <input
                      type="number"
                      min="0"
                      value={requiredPoints}
                      onChange={(e) => setRequiredPoints(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-xl border border-blue-200 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-blue-700 font-semibold"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Lượt dùng tối đa</label>
                    <input
                      type="number"
                      min="1"
                      value={usageLimit}
                      onChange={(e) => setUsageLimit(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Ngày bắt đầu *</label>
                    <input
                      type="date"
                      required
                      value={startDate}
                      onChange={(e) => setStartDate(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Ngày kết thúc *</label>
                    <input
                      type="date"
                      required
                      value={endDate}
                      onChange={(e) => setEndDate(e.target.value)}
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
                    Chương trình khuyến mãi đang hoạt động (cho phép khách hàng áp dụng mã)
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
