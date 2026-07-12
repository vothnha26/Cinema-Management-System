'use client';

import { useState, useEffect } from 'react';
import { Users, Shield, Percent, Sparkles, Search, Edit3, Save, X, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react';
import { 
  adminMembershipService, 
  MembershipBenefitData, 
  CustomerData, 
  UpdateCustomerPayload 
} from '../../../services/admin';

export default function AdminMembershipPage() {
  const [benefits, setBenefits] = useState<MembershipBenefitData[]>([]);
  const [customers, setCustomers] = useState<CustomerData[]>([]);
  const [loadingBenefits, setLoadingBenefits] = useState(true);
  const [loadingCustomers, setLoadingCustomers] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  
  // Search and Pagination
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 8;

  // Modals States
  const [isBenefitModalOpen, setIsBenefitModalOpen] = useState(false);
  const [selectedTier, setSelectedTier] = useState('');
  const [discountVal, setDiscountVal] = useState(0);
  const [multiplierVal, setMultiplierVal] = useState(1.0);

  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<CustomerData | null>(null);
  const [customerForm, setCustomerForm] = useState<UpdateCustomerPayload>({
    fullName: '',
    phone: '',
    email: '',
    membershipLevelName: 'STANDARD',
    points: 0,
    totalSpending: 0
  });

  const [submitting, setSubmitting] = useState(false);

  // Load benefits
  const fetchBenefits = async () => {
    setLoadingBenefits(true);
    try {
      const res = await adminMembershipService.getMembershipBenefits();
      if (res?.success) {
        setBenefits(res.data || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingBenefits(false);
    }
  };

  // Load customers
  const fetchCustomers = async () => {
    setLoadingCustomers(true);
    try {
      const res = await adminMembershipService.getCustomers();
      if (res?.success) {
        // Lọc bỏ GUEST
        const list = (res.data || []).filter((c: CustomerData) => c.membershipLevel && c.membershipLevel.name !== 'GUEST');
        setCustomers(list);

        // Technical Debt Performance warning
        if (list.length > 2400) {
          console.warn(`[PERFORMANCE WARNING]: Số lượng khách hàng hiện tại là ${list.length}, đã đạt hoặc vượt 80% ngưỡng tối đa (2,400/3,000). Yêu cầu đội Backend chuẩn bị phương án nâng cấp API phân trang Server-side ?page=&limit=`);
        }
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingCustomers(false);
    }
  };

  useEffect(() => {
    fetchBenefits();
    fetchCustomers();
  }, []);

  // Update benefits rules
  const handleEditBenefit = (tierName: string, discount: number, multiplier: number) => {
    setSelectedTier(tierName);
    setDiscountVal(discount);
    setMultiplierVal(multiplier);
    setIsBenefitModalOpen(true);
  };

  const handleSaveBenefit = async () => {
    setSubmitting(true);
    try {
      // Gọi RPC API song song cập nhật DISCOUNT & POINT_MULTIPLIER
      const p1 = adminMembershipService.updateBenefit(selectedTier, 'DISCOUNT', discountVal);
      const p2 = adminMembershipService.updateBenefit(selectedTier, 'POINT_MULTIPLIER', multiplierVal);
      const [r1, r2] = await Promise.all([p1, p2]);

      if (r1?.success && r2?.success) {
        alert('Cập nhật quyền lợi phân hạng hội viên thành công!');
        setIsBenefitModalOpen(false);
        fetchBenefits();
      } else {
        alert('Cập nhật thất bại: ' + (r1?.message || r2?.message || 'Lỗi không xác định'));
      }
    } catch (err: any) {
      console.error(err);
      alert('Có lỗi xảy ra khi lưu quyền lợi.');
    } finally {
      setSubmitting(false);
    }
  };

  // Update customer info
  const handleEditCustomer = (c: CustomerData) => {
    setEditingCustomer(c);
    setCustomerForm({
      fullName: c.fullName,
      phone: c.phone || '',
      email: c.email || '',
      membershipLevelName: c.membershipLevel?.name || 'STANDARD',
      points: c.points,
      totalSpending: c.totalSpending
    });
    setIsCustomerModalOpen(true);
  };

  const handleSaveCustomer = async () => {
    if (!editingCustomer) return;
    setSubmitting(true);
    try {
      const res = await adminMembershipService.updateCustomer(editingCustomer.id, customerForm);
      if (res?.success) {
        alert('Cập nhật thông tin hội viên thành công!');
        setIsCustomerModalOpen(false);
        setEditingCustomer(null);
        fetchCustomers();
      } else {
        alert(res?.message || 'Cập nhật thất bại.');
      }
    } catch (err: any) {
      console.error(err);
      alert('Có lỗi xảy ra khi lưu thông tin khách hàng.');
    } finally {
      setSubmitting(false);
    }
  };

  // Group benefits data
  const groupedBenefits = () => {
    const map: Record<string, { tier: string; discount: number; multiplier: number }> = {};
    benefits.forEach(b => {
      const tier = b.membershipLevel?.name || 'UNKNOWN';
      if (!map[tier]) {
        map[tier] = { tier, discount: 0, multiplier: 1.0 };
      }
      if (b.benefitType === 'DISCOUNT') {
        map[tier].discount = parseFloat(b.benefitValue);
      }
      if (b.benefitType === 'POINT_MULTIPLIER') {
        map[tier].multiplier = parseFloat(b.benefitValue);
      }
    });
    return Object.values(map);
  };

  // Client-side filtering & searching
  const filteredCustomers = customers.filter(c => {
    const q = searchTerm.toLowerCase();
    const nameMatch = c.fullName.toLowerCase().includes(q);
    const phoneMatch = c.phone ? c.phone.includes(q) : false;
    const emailMatch = c.email ? c.email.toLowerCase().includes(q) : false;
    const tierMatch = c.membershipLevel?.name ? c.membershipLevel.name.toLowerCase().includes(q) : false;
    return nameMatch || phoneMatch || emailMatch || tierMatch;
  });

  // Client-side pagination
  const totalPages = Math.ceil(filteredCustomers.length / pageSize);
  const paginatedCustomers = filteredCustomers.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const getTierBadgeCls = (tier: string) => {
    const map: Record<string, string> = {
      STANDARD: 'bg-gray-100 text-gray-700 border-gray-200',
      SILVER: 'bg-slate-100 text-slate-500 border-slate-200',
      GOLD: 'bg-amber-50 text-amber-600 border-amber-200',
      PLATINUM: 'bg-indigo-50 text-indigo-700 border-indigo-200'
    };
    return map[tier.toUpperCase()] || 'bg-gray-100 text-gray-700 border-gray-200';
  };

  return (
    <div className="space-y-8">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Users className="w-6 h-6 text-[#E5133A]" />
            Cấu hình Hạng & Quản lý Hội viên
          </h1>
          <p className="text-sm text-[#6B7280]">
            Thiết lập chính sách giảm giá, nhân hệ số tích điểm và quản lý hồ sơ điểm thưởng khách hàng
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => {
              fetchBenefits();
              fetchCustomers();
            }}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Tier Config Cards */}
      <div>
        <h2 className="text-base font-bold text-[#22232B] mb-4 flex items-center gap-2">
          <Shield className="w-5 h-5 text-[#F5A623]" />
          Cơ chế đặc quyền phân hạng
        </h2>

        {loadingBenefits ? (
          <div className="text-center py-8 text-sm text-[#6B7280] font-semibold">
            Đang tải cấu hình hạng...
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {groupedBenefits().map(b => (
              <div 
                key={b.tier} 
                className="bg-white border border-black/5 rounded-2xl p-6 shadow-sm relative group hover:border-[#F5A623] hover:-translate-y-1 transition-all duration-300"
              >
                <div className="flex justify-between items-start mb-4">
                  <span className={`px-3 py-1 rounded-xl text-xs font-black uppercase tracking-wider border ${getTierBadgeCls(b.tier)}`}>
                    {b.tier}
                  </span>
                </div>
                
                <div className="space-y-2 mb-6">
                  <div className="flex justify-between text-xs font-semibold text-[#6B7280]">
                    <span>Ưu đãi giảm giá:</span>
                    <strong className="text-[#22232B]">{b.discount}%</strong>
                  </div>
                  <div className="flex justify-between text-xs font-semibold text-[#6B7280]">
                    <span>Hệ số tích điểm:</span>
                    <strong className="text-[#22232B]">x{b.multiplier}</strong>
                  </div>
                </div>

                <button
                  onClick={() => handleEditBenefit(b.tier, b.discount, b.multiplier)}
                  className="w-full py-2 bg-[#FAFAFA] border border-black/5 text-[#22232B] text-xs font-bold rounded-xl hover:bg-black/5 transition-colors flex items-center justify-center gap-1.5"
                >
                  <Edit3 className="w-3.5 h-3.5" />
                  Cấu hình quyền lợi
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Customer List section */}
      <div>
        <h2 className="text-base font-bold text-[#22232B] mb-4 flex items-center gap-2">
          <Users className="w-5 h-5 text-[#E5133A]" />
          Danh sách Hội viên hệ thống
        </h2>

        {/* Filters */}
        <div className="mb-4">
          <div className="relative max-w-md">
            <Search className="w-4.5 h-4.5 absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
            <input
              type="text"
              placeholder="Tìm kiếm theo tên, số điện thoại, email, hạng..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#E5133A] text-[#22232B] placeholder:text-[#6B7280]/60 font-semibold"
            />
          </div>
        </div>

        {/* Customer Table */}
        <div className="bg-white border border-black/5 rounded-2xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[#FAFAFA] border-b border-black/5 text-xs font-bold text-[#6B7280] uppercase tracking-wider">
                  <th className="p-4 pl-6">Hội Viên</th>
                  <th className="p-4">Phân Hạng</th>
                  <th className="p-4">Điểm Tích Lũy</th>
                  <th className="p-4">Tổng Chi Tiêu</th>
                  <th className="p-4 pr-6 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 text-sm">
                {loadingCustomers ? (
                  <tr>
                    <td colSpan={5} className="p-12 text-center text-[#6B7280] font-semibold">
                      Đang tải danh sách hội viên...
                    </td>
                  </tr>
                ) : paginatedCustomers.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="p-12 text-center text-[#6B7280] font-semibold">
                      Không tìm thấy hội viên nào.
                    </td>
                  </tr>
                ) : (
                  paginatedCustomers.map((c) => (
                    <tr key={c.id} className="hover:bg-[#FAFAFA]/50 transition-colors">
                      <td className="p-4 pl-6">
                        <div className="font-bold text-[#22232B]">{c.fullName}</div>
                        <div className="text-[11px] text-[#6B7280] font-semibold">
                          {c.phone || c.email || 'N/A'}
                        </div>
                      </td>
                      <td className="p-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-lg text-[10px] font-bold border uppercase tracking-wider ${getTierBadgeCls(c.membershipLevel?.name || 'STANDARD')}`}>
                          {c.membershipLevel?.name}
                        </span>
                      </td>
                      <td className="p-4 font-bold text-amber-600">
                        {new Intl.NumberFormat('vi-VN').format(c.points)} pts
                      </td>
                      <td className="p-4 font-bold text-[#22232B]">
                        {new Intl.NumberFormat('vi-VN').format(c.totalSpending)}đ
                      </td>
                      <td className="p-4 pr-6 text-right">
                        <button
                          onClick={() => handleEditCustomer(c)}
                          className="p-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 border border-indigo-100 rounded-xl transition-all active:scale-95"
                          title="Sửa thông tin"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination bar */}
          {!loadingCustomers && totalPages > 1 && (
            <div className="p-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-between">
              <div className="text-xs text-[#6B7280] font-semibold">
                Hiển thị {(currentPage - 1) * pageSize + 1} - {Math.min(currentPage * pageSize, filteredCustomers.length)} trong số {filteredCustomers.length} hội viên
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                  disabled={currentPage === 1}
                  className="p-1.5 border border-black/10 rounded-lg text-[#6B7280] hover:bg-black/5 disabled:opacity-50 transition-colors"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="text-xs font-bold text-[#22232B] px-2">
                  Trang {currentPage} / {totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                  disabled={currentPage === totalPages}
                  className="p-1.5 border border-black/10 rounded-lg text-[#6B7280] hover:bg-black/5 disabled:opacity-50 transition-colors"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Benefit Update Modal */}
      {isBenefitModalOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white border border-black/5 rounded-2xl w-full max-w-md overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="p-5 border-b border-black/5 bg-[#FAFAFA] flex items-center justify-between">
              <h3 className="font-bold text-[#22232B] text-sm uppercase tracking-wider flex items-center gap-2">
                <Percent className="w-4.5 h-4.5 text-[#E5133A]" />
                Cập nhật quyền lợi {selectedTier}
              </h3>
              <button onClick={() => setIsBenefitModalOpen(false)}>
                <X className="w-5 h-5 text-[#6B7280] hover:text-[#22232B]" />
              </button>
            </div>
            
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                  Tỷ lệ giảm giá (%)
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  max="100"
                  value={discountVal}
                  onChange={(e) => setDiscountVal(parseFloat(e.target.value) || 0)}
                  className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                  Hệ số tích điểm (x)
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="1"
                  value={multiplierVal}
                  onChange={(e) => setMultiplierVal(parseFloat(e.target.value) || 1.0)}
                  className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                />
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsBenefitModalOpen(false)}
                  className="flex-1 py-2.5 border border-black/10 text-xs font-bold text-[#22232B] rounded-xl hover:bg-black/5 transition-all"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={handleSaveBenefit}
                  disabled={submitting}
                  className="flex-1 py-2.5 bg-gradient-to-r from-[#E5133A] to-[#8B0020] hover:opacity-95 text-white text-xs font-bold rounded-xl active:scale-[0.98] transition-all shadow-md shadow-[#E5133A]/10"
                >
                  {submitting ? 'Đang lưu...' : 'Lưu quyền lợi'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Customer Update Modal */}
      {isCustomerModalOpen && editingCustomer && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white border border-black/5 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="p-5 border-b border-black/5 bg-[#FAFAFA] flex items-center justify-between">
              <h3 className="font-bold text-[#22232B] text-sm uppercase tracking-wider flex items-center gap-2">
                <Sparkles className="w-4.5 h-4.5 text-[#F5A623]" />
                Cập nhật thông tin hội viên
              </h3>
              <button onClick={() => setIsCustomerModalOpen(false)}>
                <X className="w-5 h-5 text-[#6B7280] hover:text-[#22232B]" />
              </button>
            </div>
            
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                  Họ và tên
                </label>
                <input
                  type="text"
                  required
                  value={customerForm.fullName}
                  onChange={(e) => setCustomerForm(prev => ({ ...prev, fullName: e.target.value }))}
                  className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                    Số điện thoại
                  </label>
                  <input
                    type="text"
                    value={customerForm.phone}
                    onChange={(e) => setCustomerForm(prev => ({ ...prev, phone: e.target.value }))}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                    Email
                  </label>
                  <input
                    type="email"
                    value={customerForm.email}
                    onChange={(e) => setCustomerForm(prev => ({ ...prev, email: e.target.value }))}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                  Hạng thành viên
                </label>
                <select
                  value={customerForm.membershipLevelName}
                  onChange={(e) => setCustomerForm(prev => ({ ...prev, membershipLevelName: e.target.value }))}
                  className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A] bg-[#FAFAFA]"
                >
                  <option value="STANDARD">STANDARD</option>
                  <option value="SILVER">SILVER</option>
                  <option value="GOLD">GOLD</option>
                  <option value="PLATINUM">PLATINUM</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                    Điểm thưởng
                  </label>
                  <input
                    type="number"
                    required
                    value={customerForm.points}
                    onChange={(e) => setCustomerForm(prev => ({ ...prev, points: parseInt(e.target.value) || 0 }))}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                    Tổng chi tiêu (VNĐ)
                  </label>
                  <input
                    type="number"
                    required
                    value={customerForm.totalSpending}
                    onChange={(e) => setCustomerForm(prev => ({ ...prev, totalSpending: parseFloat(e.target.value) || 0 }))}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 text-sm font-semibold outline-none focus:border-[#E5133A]"
                  />
                </div>
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsCustomerModalOpen(false)}
                  className="flex-1 py-2.5 border border-black/10 text-xs font-bold text-[#22232B] rounded-xl hover:bg-black/5 transition-all"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={handleSaveCustomer}
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
