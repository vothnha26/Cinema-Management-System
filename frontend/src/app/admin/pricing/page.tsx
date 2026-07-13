'use client';

import { useState, useEffect } from 'react';
import { Tag, Edit, Trash, Plus, Check, X, RefreshCw, Sliders, Calendar, Clock, MapPin, Award, Monitor, ArrowUp, ArrowDown, Calculator, Settings } from 'lucide-react';
import { adminPricingService, adminBranchService, PricingRuleData, ConditionData, BranchData } from '../../../services/admin';
import { formatPrice } from '../../../utils/format';
import api from '../../../config/api';

const DAYS = [
  { id: 'MONDAY', name: 'Thứ 2' },
  { id: 'TUESDAY', name: 'Thứ 3' },
  { id: 'WEDNESDAY', name: 'Thứ 4' },
  { id: 'THURSDAY', name: 'Thứ 5' },
  { id: 'FRIDAY', name: 'Thứ 6' },
  { id: 'SATURDAY', name: 'Thứ 7' },
  { id: 'SUNDAY', name: 'Chủ Nhật' },
];

export default function AdminPricingPage() {
  const [activeTab, setActiveTab] = useState<'matrix' | 'rules'>('matrix');
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Master Data
  const [roomTypes, setRoomTypes] = useState<any[]>([]);
  const [seatTypes, setSeatTypes] = useState<any[]>([]);
  const [formats, setFormats] = useState<any[]>([]);
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [prices, setPrices] = useState<any[]>([]);
  const [rules, setRules] = useState<any[]>([]);

  // Filter chi nhánh cho Rules
  const [selectedBranchId, setSelectedBranchId] = useState<number | ''>('');

  // Modal Giá gốc
  const [showPriceModal, setShowPriceModal] = useState(false);
  const [modalRoomTypeId, setModalRoomTypeId] = useState('');
  const [modalSeatTypeId, setModalSeatTypeId] = useState('');
  const [modalRoomName, setModalRoomName] = useState('');
  const [modalSeatName, setModalSeatName] = useState('');
  const [basePriceInput, setBasePriceInput] = useState<number>(80000);

  // Modal Quy tắc phụ thu
  const [showRuleModal, setShowRuleModal] = useState(false);
  const [editRuleId, setEditRuleId] = useState<number | null>(null);
  const [ruleName, setRuleName] = useState('');
  const [ruleDescription, setRuleDescription] = useState('');
  const [ruleCategory, setRuleCategory] = useState<'BASE' | 'SURCHARGE' | 'DISCOUNT'>('SURCHARGE');
  const [ruleImpactType, setRuleImpactType] = useState<'ADDITIVE' | 'SUBTRACTIVE' | 'PERCENTAGE' | 'FIXED'>('ADDITIVE');
  const [ruleImpactValue, setRuleImpactValue] = useState<number>(0);
  const [ruleStackable, setRuleStackable] = useState(true);
  const [ruleActive, setRuleActive] = useState(true);
  const [ruleMaxDiscountLimit, setRuleMaxDiscountLimit] = useState<number>(0);
  const [ruleConditions, setRuleConditions] = useState<ConditionData[]>([]);
  const [submittingRule, setSubmittingRule] = useState(false);

  // Giả lập tính giá nhanh
  const [simBasePrice, setSimBasePrice] = useState<number>(80000);
  const [simFinalPrice, setSimFinalPrice] = useState<number>(80000);
  const [simDate, setSimDate] = useState<string>('');
  const [simTime, setSimTime] = useState<string>('19:00');
  const [simRoomType, setSimRoomType] = useState<string>('');
  const [simSeatType, setSimSeatType] = useState<string>('');
  const [simFormat, setSimFormat] = useState<string>('2D');
  const [simMemberTier, setSimMemberTier] = useState<string>('GUEST');

  const fetchInitialData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [rtRes, stRes, fRes, bRes, pRes] = await Promise.all([
        api.get('/public/master-data/room-types'),
        api.get('/public/master-data/seat-types'),
        api.get('/public/master-data/formats'),
        adminBranchService.getBranches(),
        adminPricingService.getRules(), // fetch rules/prices
      ]);

      setRoomTypes(rtRes.data?.data || []);
      const seatData = stRes.data?.data || [];
      const filteredSeatTypes = seatData.filter((s: any) => s.id !== 'EMPTY' && s.id !== 'DISABLED');
      setSeatTypes(filteredSeatTypes);
      setFormats(fRes.data?.data || []);
      setBranches(bRes?.data || []);

      // Lấy toàn bộ giá gốc (từ api /api/admin/pricing)
      const basePricesRes = await api.get('/admin/pricing');
      setPrices(basePricesRes.data?.data || []);

      // Set mặc định cho simulator
      if (rtRes.data?.data?.length > 0) setSimRoomType(rtRes.data.data[0].id);
      if (filteredSeatTypes.length > 0) setSimSeatType(filteredSeatTypes[0].id);
    } catch (err: any) {
      console.error(err);
      setErrorMsg('Không thể tải cấu hình giá vé.');
    } finally {
      setLoading(false);
    }
  };

  const fetchRules = async () => {
    try {
      const res = await adminPricingService.getRules(selectedBranchId ? Number(selectedBranchId) : undefined);
      if (res?.success) {
        setRules(res.data || []);
      }
    } catch (err) {
      console.error('Lỗi tải quy tắc phụ thu:', err);
    }
  };

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    fetchRules();
  }, [selectedBranchId]);

  // Giả lập giá khi có bất kỳ tham số nào thay đổi
  useEffect(() => {
    runSimulator();
  }, [simBasePrice, simDate, simTime, simRoomType, simSeatType, simFormat, simMemberTier, rules]);

  const runSimulator = () => {
    let current = simBasePrice;
    const sorted = [...rules].filter((r) => r.active).sort((a, b) => a.priority - b.priority);

    // Xác định thứ trong tuần từ ngày chiếu
    let dayOfWeek = '';
    if (simDate) {
      const dateObj = new Date(simDate);
      const dayIndex = dateObj.getDay(); // 0: CN, 1: Thứ 2
      const dayMapping = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
      dayOfWeek = dayMapping[dayIndex];
    }

    for (const rule of sorted) {
      const matches = (rule.conditions || []).every((c: any) => {
        if (c.type === 'ROOM_TYPE' && c.value !== simRoomType) return false;
        if (c.type === 'SEAT_TYPE' && c.value !== simSeatType) return false;
        if (c.type === 'SHOW_FORMAT' && c.value !== simFormat) return false;
        if (c.type === 'MEMBER_TIER' && c.value !== simMemberTier) return false;
        if (c.type === 'BRANCH' && selectedBranchId && c.value != selectedBranchId) return false;

        if (c.type === 'DAY_OF_WEEK') {
          const days = c.value.split(',');
          if (dayOfWeek && !days.includes(dayOfWeek)) return false;
        }

        if (c.type === 'TIME_RANGE') {
          const [start, end] = c.value.split('-');
          if (simTime < start || simTime > end) return false;
        }

        if (c.type === 'DATE_RANGE' && simDate) {
          const [start, end] = c.value.split(':');
          if (simDate < start || simDate > end) return false;
        }

        return true;
      });

      if (matches) {
        if (rule.impactType === 'FIXED') current = Number(rule.impactValue);
        else if (rule.impactType === 'ADDITIVE') current += Number(rule.impactValue);
        else if (rule.impactType === 'SUBTRACTIVE') current -= Number(rule.impactValue);
        else if (rule.impactType === 'PERCENTAGE') current *= Number(rule.impactValue);
        if (!rule.stackable) break;
      }
    }
    setSimFinalPrice(Math.max(0, Math.round(current)));
  };

  // --- Base Matrix actions ---
  const handleOpenPriceModal = (roomId: string, seatId: string, roomName: string, seatName: string, priceVal: number) => {
    setModalRoomTypeId(roomId);
    setModalSeatTypeId(seatId);
    setModalRoomName(roomName);
    setModalSeatName(seatName);
    setBasePriceInput(priceVal);
    setShowPriceModal(true);
  };

  const handleSavePrice = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.put('/admin/pricing', {
        roomTypeId: modalRoomTypeId,
        seatTypeId: modalSeatTypeId,
        price: basePriceInput,
      });
      if (res.data?.success) {
        setShowPriceModal(false);
        fetchInitialData();
      } else {
        alert(res.data?.message || 'Cập nhật thất bại!');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi khi lưu giá vé.');
    }
  };

  // --- Rule actions ---
  const handleOpenCreateRule = () => {
    setEditRuleId(null);
    setRuleName('');
    setRuleDescription('');
    setRuleCategory('SURCHARGE');
    setRuleImpactType('ADDITIVE');
    setRuleImpactValue(0);
    setRuleStackable(true);
    setRuleActive(true);
    setRuleMaxDiscountLimit(0);
    setRuleConditions([]);
    setShowRuleModal(true);
  };

  const handleOpenEditRule = (rule: any) => {
    setEditRuleId(rule.id);
    setRuleName(rule.name);
    setRuleDescription(rule.description || '');
    setRuleCategory(rule.category);
    setRuleImpactType(rule.impactType);
    setRuleImpactValue(rule.impactValue);
    setRuleStackable(rule.stackable !== false);
    setRuleActive(rule.active !== false);
    setRuleMaxDiscountLimit(rule.maxDiscountLimit || 0);
    setRuleConditions(rule.conditions || []);
    setShowRuleModal(true);
  };

  const handleAddCondition = () => {
    setRuleConditions([...ruleConditions, { type: 'ROOM_TYPE', value: '' }]);
  };

  const handleRemoveCondition = (index: number) => {
    setRuleConditions(ruleConditions.filter((_, i) => i !== index));
  };

  const handleConditionChange = (index: number, field: keyof ConditionData, val: string) => {
    const updated = [...ruleConditions];
    updated[index] = { ...updated[index], [field]: val };
    setRuleConditions(updated);
  };

  const handleSaveRule = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmittingRule(true);

    const ruleData: PricingRuleData = {
      name: ruleName.trim(),
      description: ruleDescription.trim(),
      category: ruleCategory,
      impactType: ruleImpactType,
      impactValue: ruleImpactValue,
      priority: editRuleId ? rules.find((r) => r.id === editRuleId)?.priority || 100 : 100,
      active: ruleActive,
      stackable: ruleStackable,
      maxDiscountLimit: ruleCategory === 'DISCOUNT' ? ruleMaxDiscountLimit : 0,
      conditions: ruleConditions.filter((c) => c.value !== ''),
    };

    try {
      let res;
      if (editRuleId !== null) {
        res = await adminPricingService.updateRule(editRuleId, ruleData);
      } else {
        res = await adminPricingService.createRule(ruleData);
      }

      if (res?.success) {
        setShowRuleModal(false);
        fetchRules();
      } else {
        alert(res?.message || 'Lưu quy tắc thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    } finally {
      setSubmittingRule(false);
    }
  };

  const handleDeleteRule = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa quy tắc giá này?')) return;
    try {
      const res = await adminPricingService.deleteRule(id);
      if (res?.success) {
        fetchRules();
      } else {
        alert(res?.message || 'Xóa quy tắc thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    }
  };

  const handleMoveRule = async (index: number, direction: 'up' | 'down') => {
    const newRules = [...rules];
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= newRules.length) return;

    // Swap priority
    const temp = newRules[index];
    newRules[index] = newRules[targetIndex];
    newRules[targetIndex] = temp;

    // Cập nhật lại priority tăng dần từ 1
    const ruleIds = newRules.map((r) => r.id);
    try {
      const res = await adminPricingService.reorderRules(ruleIds, selectedBranchId ? Number(selectedBranchId) : undefined);
      if (res?.success) {
        fetchRules();
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Sliders className="w-6 h-6 text-[#F5A623]" />
            Ma Trận Giá Vé & Chiến Lược Phụ Thu
          </h1>
          <p className="text-sm text-[#6B7280]">Cấu hình bảng giá gốc của phòng/ghế và tháp ưu tiên quy tắc tính giá vé tự động</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchInitialData}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          {activeTab === 'rules' && (
            <button
              onClick={handleOpenCreateRule}
              className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
            >
              <Plus className="w-5 h-5" />
              Tạo quy tắc mới
            </button>
          )}
        </div>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-black/5 gap-6">
        <button
          onClick={() => setActiveTab('matrix')}
          className={`py-3 text-sm font-bold border-b-2 transition-all ${
            activeTab === 'matrix' ? 'border-[#F5A623] text-[#F5A623]' : 'border-transparent text-[#6B7280] hover:text-[#22232B]'
          }`}
        >
          MA TRẬN GIÁ VÉ GỐC
        </button>
        <button
          onClick={() => setActiveTab('rules')}
          className={`py-3 text-sm font-bold border-b-2 transition-all ${
            activeTab === 'rules' ? 'border-[#F5A623] text-[#F5A623]' : 'border-transparent text-[#6B7280] hover:text-[#22232B]'
          }`}
        >
          QUY TẮC PHỤ THU & ƯU TIÊN
        </button>
      </div>

      {loading ? (
        <div className="bg-white border border-black/5 rounded-2xl p-12 text-center text-[#6B7280] font-medium shadow-sm">
          Đang tải dữ liệu chính sách giá vé...
        </div>
      ) : activeTab === 'matrix' ? (
        /* TAB 1: BASE MATRIX */
        <div className="bg-white border border-black/5 rounded-2xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-center border-collapse">
              <thead>
                <tr className="bg-[#FAFAFA] border-b border-black/5">
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider text-left">Phòng \ Ghế</th>
                  {seatTypes.map((seat) => (
                    <th key={seat.id} className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">
                      {seat.name}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5">
                {roomTypes.map((room) => (
                  <tr key={room.id} className="hover:bg-black/[0.01] transition-colors">
                    <td className="px-6 py-4 text-left font-bold text-[#22232B]">{room.name}</td>
                    {seatTypes.map((seat) => {
                      const priceObj = prices.find((p) => p.roomTypeId === room.id && p.seatTypeId === seat.id);
                      const basePrice = priceObj ? priceObj.price : 80000;

                      return (
                        <td key={seat.id} className="px-6 py-4">
                          <button
                            onClick={() => handleOpenPriceModal(room.id, seat.id, room.name, seat.name, basePrice)}
                            className="group px-4 py-2 border border-black/5 rounded-xl hover:border-[#F5A623] hover:bg-[#F5A623]/5 transition-all text-[#F5A623] font-bold font-mono text-sm inline-flex items-center gap-1.5"
                          >
                            {formatPrice(basePrice)}
                            <Edit className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 text-[#6B7280]" />
                          </button>
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* TAB 2: ADVANCED RULES & SIMULATOR */
        <div className="space-y-6">
          {/* Quick Pricing Simulator */}
          <div className="bg-amber-50/50 border border-amber-200/60 rounded-2xl p-5 space-y-4">
            <h3 className="font-bold text-sm text-amber-800 flex items-center gap-1.5">
              <Calculator className="w-5 h-5" />
              BỘ GIẢ LẬP TÍNH GIÁ VÉ DỰ KIẾN (QUICK SIMULATOR)
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Giá vé gốc (VNĐ)</label>
                <input
                  type="number"
                  value={simBasePrice}
                  onChange={(e) => setSimBasePrice(Number(e.target.value))}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B] font-bold"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Ngày chiếu</label>
                <input
                  type="date"
                  value={simDate}
                  onChange={(e) => setSimDate(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Giờ chiếu</label>
                <input
                  type="time"
                  value={simTime}
                  onChange={(e) => setSimTime(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Loại phòng</label>
                <select
                  value={simRoomType}
                  onChange={(e) => setSimRoomType(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                >
                  {roomTypes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Loại ghế</label>
                <select
                  value={simSeatType}
                  onChange={(e) => setSimSeatType(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                >
                  {seatTypes.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Định dạng</label>
                <select
                  value={simFormat}
                  onChange={(e) => setSimFormat(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                >
                  <option value="2D">2D</option>
                  <option value="3D">3D</option>
                  <option value="IMAX">IMAX</option>
                  <option value="4DX">4DX</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-amber-700 uppercase tracking-wider mb-1">Hạng thành viên</label>
                <select
                  value={simMemberTier}
                  onChange={(e) => setSimMemberTier(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                >
                  <option value="GUEST">GUEST</option>
                  <option value="STANDARD">STANDARD</option>
                  <option value="SILVER">SILVER</option>
                  <option value="GOLD">GOLD</option>
                  <option value="PLATINUM">PLATINUM</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-green-700 uppercase tracking-wider mb-1">Giá vé cuối tính toán</label>
                <div className="px-3 py-2 rounded-xl bg-green-500 text-white font-extrabold text-lg font-mono text-center">
                  {formatPrice(simFinalPrice)}
                </div>
              </div>
            </div>
          </div>

          {/* Quy tắc giá */}
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Thứ tự tháp ưu tiên các quy tắc</span>
            {/* Filter chi nhánh */}
            <div className="flex items-center gap-2">
              <span className="text-xs text-[#6B7280] font-semibold">Bộ lọc rạp:</span>
              <select
                value={selectedBranchId}
                onChange={(e) => setSelectedBranchId(e.target.value ? Number(e.target.value) : '')}
                className="px-3 py-1.5 rounded-lg border border-black/10 text-xs font-semibold text-[#22232B] bg-white outline-none"
              >
                <option value="">Tất cả hệ thống</option>
                {branches.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="space-y-3">
            {rules.map((rule, idx) => {
              const hasBranchCond = (rule.conditions || []).some((c: any) => c.type === 'BRANCH');
              const impactSign =
                rule.impactType === 'ADDITIVE'
                  ? '+'
                  : rule.impactType === 'SUBTRACTIVE'
                  ? '-'
                  : rule.impactType === 'PERCENTAGE'
                  ? 'x'
                  : '=';
              const impactVal = rule.impactType === 'PERCENTAGE' ? rule.impactValue : formatPrice(rule.impactValue);

              return (
                <div
                  key={rule.id}
                  className={`bg-white border rounded-2xl p-4 flex items-center justify-between transition-all ${
                    rule.active ? 'border-black/5 shadow-sm' : 'border-dashed border-gray-300 opacity-60'
                  }`}
                >
                  <div className="flex items-start gap-4">
                    {/* Controls di chuyển tháp ưu tiên */}
                    <div className="flex flex-col gap-1">
                      <button
                        onClick={() => handleMoveRule(idx, 'up')}
                        disabled={idx === 0}
                        className="p-1 rounded bg-gray-50 border border-black/5 hover:bg-black/5 disabled:opacity-50 text-[#6B7280]"
                      >
                        <ArrowUp className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleMoveRule(idx, 'down')}
                        disabled={idx === rules.length - 1}
                        className="p-1 rounded bg-gray-50 border border-black/5 hover:bg-black/5 disabled:opacity-50 text-[#6B7280]"
                      >
                        <ArrowDown className="w-3.5 h-3.5" />
                      </button>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 flex-wrap mb-1.5">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                            rule.category === 'BASE'
                              ? 'bg-blue-50 text-blue-700 border border-blue-200'
                              : rule.category === 'SURCHARGE'
                              ? 'bg-amber-50 text-amber-700 border border-amber-200'
                              : 'bg-green-50 text-green-700 border border-green-200'
                          }`}
                        >
                          {rule.category === 'BASE' ? 'Giá gốc' : rule.category === 'SURCHARGE' ? 'Phụ thu' : 'Giảm giá'}
                        </span>
                        <span className="px-2 py-0.5 rounded bg-gray-100 text-gray-700 border border-gray-200 text-[10px] font-bold">
                          Ưu tiên #{idx + 1}
                        </span>
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                            hasBranchCond ? 'bg-purple-50 text-purple-700' : 'bg-red-50 text-red-700'
                          }`}
                        >
                          {hasBranchCond ? 'Chi nhánh' : 'Toàn hệ thống'}
                        </span>
                        {!rule.stackable && (
                          <span className="px-2 py-0.5 rounded bg-red-100 text-red-800 text-[10px] font-bold">
                            Chặn cộng dồn
                          </span>
                        )}
                      </div>

                      <h4 className="font-bold text-sm text-[#22232B]">{rule.name}</h4>
                      <p className="text-xs text-[#6B7280]">{rule.description || 'Không có mô tả.'}</p>

                      {/* Hiển thị danh sách điều kiện áp dụng */}
                      {rule.conditions && rule.conditions.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 mt-2">
                          {rule.conditions.map((c: any, cIdx: number) => (
                            <span
                              key={cIdx}
                              className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-[#FAFAFA] border border-black/5 text-[10px] text-[#6B7280] font-semibold"
                            >
                              {c.type === 'DAY_OF_WEEK' && <Calendar className="w-3 h-3 text-[#F5A623]" />}
                              {c.type === 'TIME_RANGE' && <Clock className="w-3 h-3 text-[#F5A623]" />}
                              {c.type === 'BRANCH' && <MapPin className="w-3 h-3 text-[#F5A623]" />}
                              {c.type === 'MEMBER_TIER' && <Award className="w-3 h-3 text-[#F5A623]" />}
                              {c.type === 'SHOW_FORMAT' && <Monitor className="w-3 h-3 text-[#F5A623]" />}
                              {c.type}: {c.value}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <div className="text-lg font-bold font-mono text-[#F5A623]">
                        {impactSign}
                        {impactVal}
                      </div>
                      {rule.category === 'DISCOUNT' && rule.maxDiscountLimit > 0 && (
                        <div className="text-[10px] text-[#6B7280]">Trần: {formatPrice(rule.maxDiscountLimit)}</div>
                      )}
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleOpenEditRule(rule)}
                        className="p-2 border border-black/10 rounded-xl text-[#6B7280] hover:text-[#F5A623] hover:border-[#F5A623] bg-white transition-all"
                        title="Sửa quy tắc"
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDeleteRule(rule.id)}
                        className="p-2 border border-black/10 rounded-xl text-[#EF4444] hover:bg-red-500/5 hover:border-red-200 bg-white transition-all"
                        title="Xóa quy tắc"
                      >
                        <Trash className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Modal Cập nhật giá gốc */}
      {showPriceModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-base text-[#22232B]">Cập nhật Giá Vé Gốc</h3>
              <button
                onClick={() => setShowPriceModal(false)}
                className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280] transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSavePrice}>
              <div className="p-6 space-y-4">
                <div>
                  <span className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-1">Loại Phòng & Loại Ghế</span>
                  <div className="text-sm font-bold text-red-500">
                    {modalRoomName} - {modalSeatName}
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giá vé cơ bản (VNĐ)</label>
                  <input
                    type="number"
                    required
                    min="0"
                    value={basePriceInput}
                    onChange={(e) => setBasePriceInput(Number(e.target.value))}
                    className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-mono font-bold"
                  />
                </div>
              </div>

              <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowPriceModal(false)}
                  className="px-4 py-2 border border-black/10 rounded-xl font-semibold text-[#6B7280] hover:bg-black/5 text-sm"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-600 text-white rounded-xl font-bold hover:bg-red-700 text-sm shadow-md"
                >
                  Lưu thay đổi
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Thêm/Sửa Quy tắc giá advanced */}
      {showRuleModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-base text-[#22232B]">
                {editRuleId !== null ? 'Cập nhật Quy tắc Giá' : 'Tạo Quy tắc Giá mới'}
              </h3>
              <button
                onClick={() => setShowRuleModal(false)}
                className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280] transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSaveRule}>
              <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="sm:col-span-2">
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên quy tắc *</label>
                    <input
                      type="text"
                      required
                      value={ruleName}
                      onChange={(e) => setRuleName(e.target.value)}
                      placeholder="VD: Phụ thu cuối tuần (Sat & Sun)"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Loại quy tắc</label>
                    <select
                      value={ruleCategory}
                      onChange={(e) => setRuleCategory(e.target.value as any)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                    >
                      <option value="SURCHARGE">Phụ thu (SURCHARGE)</option>
                      <option value="DISCOUNT">Khuyến mãi / Giảm giá (DISCOUNT)</option>
                      <option value="BASE">Cơ bản (BASE)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Loại tác động</label>
                    <select
                      value={ruleImpactType}
                      onChange={(e) => setRuleImpactType(e.target.value as any)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                    >
                      <option value="ADDITIVE">Cộng tiền (+)</option>
                      <option value="SUBTRACTIVE">Trừ tiền (-)</option>
                      <option value="PERCENTAGE">Nhân tỉ lệ (x)</option>
                      <option value="FIXED">Thiết lập giá cố định (=)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giá trị tác động *</label>
                    <input
                      type="number"
                      required
                      step="0.01"
                      value={ruleImpactValue}
                      onChange={(e) => setRuleImpactValue(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-mono font-bold"
                    />
                  </div>

                  {ruleCategory === 'DISCOUNT' ? (
                    <div>
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giảm tối đa (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={ruleMaxDiscountLimit}
                        onChange={(e) => setRuleMaxDiscountLimit(Number(e.target.value))}
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

                  <div className="sm:col-span-2">
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Mô tả ngắn</label>
                    <textarea
                      value={ruleDescription}
                      onChange={(e) => setRuleDescription(e.target.value)}
                      rows={2}
                      placeholder="VD: Quy tắc phụ thu 15,000 VNĐ cho các suất chiếu cuối tuần"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>
                </div>

                {/* Switches */}
                <div className="flex flex-wrap gap-6 pt-2">
                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      id="ruleStackable"
                      checked={ruleStackable}
                      onChange={(e) => setRuleStackable(e.target.checked)}
                      className="w-4.5 h-4.5 text-[#F5A623] border-black/10 rounded focus:ring-[#F5A623] accent-[#F5A623]"
                    />
                    <label htmlFor="ruleStackable" className="text-sm font-semibold text-[#22232B] select-none cursor-pointer">
                      Cho phép cộng dồn với quy tắc khác
                    </label>
                  </div>

                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      id="ruleActive"
                      checked={ruleActive}
                      onChange={(e) => setRuleActive(e.target.checked)}
                      className="w-4.5 h-4.5 text-[#F5A623] border-black/10 rounded focus:ring-[#F5A623] accent-[#F5A623]"
                    />
                    <label htmlFor="ruleActive" className="text-sm font-semibold text-green-700 select-none cursor-pointer">
                      Quy tắc đang chạy
                    </label>
                  </div>
                </div>

                {/* Điều kiện áp dụng */}
                <div className="border border-black/5 rounded-2xl p-4 bg-[#FAFAFA] space-y-3">
                  <div className="flex items-center justify-between border-b border-black/5 pb-2">
                    <h4 className="text-xs font-bold text-[#6B7280] uppercase tracking-wider flex items-center gap-1.5">
                      <Settings className="w-4 h-4 text-[#F5A623]" /> Điều kiện áp dụng quy tắc
                    </h4>
                    <button
                      type="button"
                      onClick={handleAddCondition}
                      className="text-xs font-bold text-[#F5A623] hover:underline"
                    >
                      + Thêm điều kiện
                    </button>
                  </div>

                  {ruleConditions.length === 0 ? (
                    <div className="text-center py-4 text-xs text-[#6B7280] italic">
                      Chưa cấu hình điều kiện (Quy tắc áp dụng cho mọi trường hợp)
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {ruleConditions.map((cond, index) => (
                        <div key={index} className="flex items-center gap-3 bg-white p-3 border border-black/5 rounded-xl">
                          <select
                            value={cond.type}
                            onChange={(e) => handleConditionChange(index, 'type', e.target.value)}
                            className="px-2 py-1.5 rounded-lg border border-black/10 text-xs font-semibold text-[#22232B] bg-white outline-none"
                          >
                            <option value="ROOM_TYPE">Loại Phòng</option>
                            <option value="SEAT_TYPE">Loại Ghế</option>
                            <option value="DAY_OF_WEEK">Thứ trong tuần</option>
                            <option value="TIME_RANGE">Khoảng thời gian (Giờ)</option>
                            <option value="DATE_RANGE">Khoảng ngày</option>
                            <option value="MEMBER_TIER">Hạng thành viên</option>
                            <option value="SHOW_FORMAT">Định dạng chiếu</option>
                            <option value="BRANCH">Chi nhánh rạp</option>
                          </select>

                          {/* Nhập value tương ứng với loại điều kiện */}
                          <div className="flex-grow">
                            {cond.type === 'DAY_OF_WEEK' ? (
                              <input
                                type="text"
                                required
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                placeholder="VD: SATURDAY,SUNDAY"
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              />
                            ) : cond.type === 'TIME_RANGE' ? (
                              <input
                                type="text"
                                required
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                placeholder="VD: 18:00-23:00"
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              />
                            ) : cond.type === 'DATE_RANGE' ? (
                              <input
                                type="text"
                                required
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                placeholder="VD: 2026-01-01:2026-01-07"
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              />
                            ) : cond.type === 'MEMBER_TIER' ? (
                              <select
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              >
                                <option value="">Chọn hạng...</option>
                                <option value="GUEST">GUEST</option>
                                <option value="STANDARD">STANDARD</option>
                                <option value="SILVER">SILVER</option>
                                <option value="GOLD">GOLD</option>
                                <option value="PLATINUM">PLATINUM</option>
                              </select>
                            ) : cond.type === 'SHOW_FORMAT' ? (
                              <select
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              >
                                <option value="">Chọn định dạng...</option>
                                <option value="2D">2D</option>
                                <option value="3D">3D</option>
                                <option value="IMAX">IMAX</option>
                                <option value="4DX">4DX</option>
                              </select>
                            ) : cond.type === 'BRANCH' ? (
                              <select
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              >
                                <option value="">Chọn chi nhánh...</option>
                                {branches.map((b) => (
                                  <option key={b.id} value={b.id?.toString()}>
                                    {b.name}
                                  </option>
                                ))}
                              </select>
                            ) : cond.type === 'ROOM_TYPE' ? (
                              <select
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              >
                                <option value="">Chọn loại phòng...</option>
                                {roomTypes.map((rt) => (
                                  <option key={rt.id} value={rt.id}>
                                    {rt.name}
                                  </option>
                                ))}
                              </select>
                            ) : cond.type === 'SEAT_TYPE' ? (
                              <select
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              >
                                <option value="">Chọn loại ghế...</option>
                                {seatTypes.map((st) => (
                                  <option key={st.id} value={st.id}>
                                    {st.name}
                                  </option>
                                ))}
                              </select>
                            ) : (
                              <input
                                type="text"
                                required
                                value={cond.value}
                                onChange={(e) => handleConditionChange(index, 'value', e.target.value)}
                                placeholder="Nhập giá trị điều kiện..."
                                className="w-full px-3 py-1.5 rounded-lg border border-black/10 text-xs text-[#22232B]"
                              />
                            )}
                          </div>

                          <button
                            type="button"
                            onClick={() => handleRemoveCondition(index)}
                            className="p-1 text-[#EF4444] hover:bg-red-50 rounded-lg"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowRuleModal(false)}
                  className="px-5 py-2.5 border border-black/10 rounded-xl font-semibold text-[#6B7280] hover:bg-black/5 text-sm"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={submittingRule}
                  className="px-5 py-2.5 bg-red-600 text-white rounded-xl font-bold hover:bg-red-700 text-sm shadow-md flex items-center gap-2 active:scale-[0.98]"
                >
                  <Check className="w-4 h-4" />
                  {submittingRule ? 'Đang lưu...' : 'Lưu quy tắc'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
