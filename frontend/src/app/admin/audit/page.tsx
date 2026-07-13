'use client';

import { useState, useEffect } from 'react';
import { ShieldAlert, Search, RefreshCw, ChevronLeft, ChevronRight, Calendar, User, Activity } from 'lucide-react';
import { adminAuditService, AuditLogData } from '../../../services/admin';

export default function AdminAuditPage() {
  const [logs, setLogs] = useState<AuditLogData[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  
  // Search and Pagination
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 12;

  const fetchLogs = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminAuditService.getAuditLogs();
      if (res?.success) {
        const list = Array.isArray(res.data) ? res.data : [];
        setLogs(list);

        // Technical Debt Performance Warning
        if (list.length > 4000) {
          console.warn(`[PERFORMANCE WARNING]: Số lượng bản ghi nhật ký hệ thống hiện tại là ${list.length}, đã đạt hoặc vượt 80% ngưỡng tối đa (4,000/5,000). Yêu cầu đội Backend chuẩn bị phương án nâng cấp API phân trang Server-side ?page=&limit=`);
        }
      } else {
        setErrorMsg(res?.message || 'Không thể lấy nhật ký hệ thống.');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg('Lỗi xảy ra khi tải nhật ký hoạt động.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  // Client-side searching
  const filteredLogs = (Array.isArray(logs) ? logs : []).filter(log => {
    const q = searchTerm.toLowerCase();
    const usernameMatch = log.username ? log.username.toLowerCase().includes(q) : false;
    const actionMatch = log.action ? log.action.toLowerCase().includes(q) : false;
    const targetMatch = log.target ? log.target.toLowerCase().includes(q) : false;
    const detailMatch = log.detail ? log.detail.toLowerCase().includes(q) : false;
    return usernameMatch || actionMatch || targetMatch || detailMatch;
  });

  // Client-side pagination
  const totalPages = Math.ceil(filteredLogs.length / pageSize);
  const paginatedLogs = filteredLogs.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  // Styling action badge
  const getActionBadgeCls = (action: string) => {
    const act = action.toUpperCase();
    if (act.includes('CREATE')) {
      return 'bg-green-50 text-green-700 border-green-200';
    }
    if (act.includes('UPDATE') || act.includes('EDIT')) {
      return 'bg-blue-50 text-blue-700 border-blue-200';
    }
    if (act.includes('DELETE') || act.includes('REMOVE')) {
      return 'bg-red-50 text-red-700 border-red-200';
    }
    return 'bg-gray-50 text-gray-700 border-gray-200';
  };

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <ShieldAlert className="w-6 h-6 text-[#E5133A]" />
            Nhật ký & Kiểm toán hệ thống
          </h1>
          <p className="text-sm text-[#6B7280]">
            Theo dõi tất cả lịch sử thay đổi thông tin nhạy cảm của hệ thống do quản trị viên và nhân viên thực hiện
          </p>
        </div>

        <button
          onClick={fetchLogs}
          className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors flex items-center gap-2 text-xs font-bold"
          title="Làm mới"
        >
          <RefreshCw className="w-4 h-4" />
          LÀM MỚI LOGS
        </button>
      </div>

      {/* Search Filter */}
      <div className="relative max-w-md">
        <Search className="w-4.5 h-4.5 absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
        <input
          type="text"
          placeholder="Tìm kiếm theo người dùng, hành động, đối tượng, chi tiết..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setCurrentPage(1);
          }}
          className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#E5133A] text-[#22232B] placeholder:text-[#6B7280]/60 font-semibold"
        />
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Logs Table Grid */}
      <div className="bg-white border border-black/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#FAFAFA] border-b border-black/5 text-xs font-bold text-[#6B7280] uppercase tracking-wider">
                <th className="p-4 pl-6">Thời Gian</th>
                <th className="p-4">Người Thực Hiện</th>
                <th className="p-4">Hành Động</th>
                <th className="p-4">Đối Tượng</th>
                <th className="p-4 pr-6">Chi Tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-black/5 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={5} className="p-12 text-center text-[#6B7280] font-semibold">
                    Đang tải nhật ký kiểm toán...
                  </td>
                </tr>
              ) : paginatedLogs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-12 text-center text-[#6B7280] font-semibold">
                    Chưa có hoạt động nào được ghi lại.
                  </td>
                </tr>
              ) : (
                paginatedLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-[#FAFAFA]/50 transition-colors">
                    <td className="p-4 pl-6 text-[#6B7280] font-medium min-w-[170px]">
                      <span className="flex items-center gap-1.5 text-xs">
                        <Calendar className="w-3.5 h-3.5" />
                        {new Date(log.timestamp).toLocaleString('vi-VN')}
                      </span>
                    </td>
                    <td className="p-4 font-bold text-[#22232B] min-w-[140px]">
                      <span className="flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5 text-[#6B7280]/60" />
                        {log.username}
                      </span>
                    </td>
                    <td className="p-4 min-w-[120px]">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-lg text-[10px] font-bold border uppercase tracking-wider ${getActionBadgeCls(log.action)}`}>
                        <Activity className="w-3 h-3 mr-1" />
                        {log.action}
                      </span>
                    </td>
                    <td className="p-4 font-semibold text-[#22232B] min-w-[150px]">
                      {log.target}
                    </td>
                    <td className="p-4 pr-6 text-xs font-mono text-[#6B7280] max-w-sm truncate" title={log.detail}>
                      {log.detail}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination bar */}
        {!loading && totalPages > 1 && (
          <div className="p-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-between">
            <div className="text-xs text-[#6B7280] font-semibold">
              Hiển thị {(currentPage - 1) * pageSize + 1} - {Math.min(currentPage * pageSize, filteredLogs.length)} trong số {filteredLogs.length} bản ghi
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
  );
}
