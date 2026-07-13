'use client';

import { useState, useEffect } from 'react';
import { Plus, Pencil, Trash, X, Save, Users, Key, Briefcase, Mail, RefreshCw, ShieldAlert, Check, Shield } from 'lucide-react';
import { adminUserService, adminBranchService, BranchData } from '../../../services/admin';
import { USER_ROLES } from '../../../constants';

export default function AdminUsersPage() {
  const [users, setUsers] = useState<any[]>([]);
  const [branches, setBranches] = useState<BranchData[]>([]);
  const [staffAssignments, setStaffAssignments] = useState<Record<number, any>>({});
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Trạng thái modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);

  // Form values
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState(''); // Chỉ dùng khi tạo mới
  const [role, setRole] = useState<'STAFF' | 'MANAGER' | 'ADMIN'>('STAFF');

  // Staff Assignment form values (nếu role != ADMIN)
  const [branchId, setBranchId] = useState<number | ''>('');
  const [staffCode, setStaffCode] = useState('');
  const [position, setPosition] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchInitialData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      // 1. Lấy danh sách chi nhánh
      const branchRes = await adminBranchService.getBranches();
      if (branchRes?.success) {
        setBranches(branchRes.data || []);
        if (branchRes.data.length > 0) setBranchId(branchRes.data[0].id);
      }

      // 2. Lấy phân công chi nhánh nhân sự
      const assignmentsRes = await adminUserService.getStaffAssignments();
      if (assignmentsRes?.success) {
        setStaffAssignments(assignmentsRes.data || {});
      }

      // 3. Lấy tất cả user (ở client ta filter loại bỏ CUSTOMER để chỉ hiển thị nhân sự hệ thống)
      const userRes = await adminUserService.getUsers();
      if (userRes?.success) {
        const userDataList = Array.isArray(userRes.data) ? userRes.data : [];
        const staffUsers = userDataList.filter((u: any) => u.role !== USER_ROLES.CUSTOMER);
        setUsers(staffUsers);
      } else {
        setErrorMsg(userRes?.message || 'Không thể tải danh sách nhân sự!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInitialData();
  }, []);

  const handleOpenCreateModal = () => {
    setEditId(null);
    setUsername('');
    setEmail('');
    setPassword('');
    setRole('STAFF');
    setStaffCode('');
    setPosition('');
    if (branches.length > 0) setBranchId(branches[0].id!);
    setShowModal(true);
  };

  const handleOpenEditModal = (userItem: any) => {
    setEditId(userItem.id);
    setUsername(userItem.username);
    setEmail(userItem.email);
    setPassword('');
    setRole(userItem.role);

    const assignment = staffAssignments[userItem.id];
    if (assignment) {
      setBranchId(assignment.branch?.id || '');
      setStaffCode(assignment.staffCode || '');
      setPosition(assignment.position || '');
    } else {
      setStaffCode('');
      setPosition('');
      if (branches.length > 0) setBranchId(branches[0].id!);
    }
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (editId !== null) {
        // Cập nhật tài khoản
        const res = await adminUserService.updateUser(editId, { email, role });
        if (res?.success) {
          // Nếu role không phải ADMIN, cập nhật thêm chi nhánh làm việc
          if (role !== USER_ROLES.ADMIN && branchId !== '') {
            await adminUserService.assignStaffToBranch({
              userId: editId,
              branchId: Number(branchId),
              staffCode,
              position,
            });
          }
          setShowModal(false);
          fetchInitialData();
        } else {
          alert(res?.message || 'Cập nhật thất bại!');
        }
      } else {
        // Tạo tài khoản mới (tạo staff)
        const staffRequestData = {
          username,
          email,
          password: password || '123456', // default pass
          role,
        };

        const res = await adminUserService.createStaff(staffRequestData);
        if (res?.success) {
          const newUser = res.data;
          // Phân công chi nhánh
          if (role !== USER_ROLES.ADMIN && branchId !== '') {
            await adminUserService.assignStaffToBranch({
              userId: newUser.id,
              branchId: Number(branchId),
              staffCode,
              position,
            });
          }
          setShowModal(false);
          fetchInitialData();
        } else {
          alert(res?.message || 'Tạo nhân viên thất bại!');
        }
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi xảy ra khi lưu thông tin nhân viên.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản nhân sự này? Thao tác này không thể hoàn tác!')) return;
    try {
      const res = await adminUserService.deleteUser(id);
      if (res?.success) {
        fetchInitialData();
      } else {
        alert(res?.message || 'Xóa tài khoản thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi khi xóa nhân sự.');
    }
  };

  const handleToggleStatus = async (id: number) => {
    try {
      const res = await adminUserService.toggleUserStatus(id);
      if (res?.success) {
        // Cập nhật nhanh local state để tránh load lại cả trang
        setUsers(users.map((u) => (u.id === id ? { ...u, status: !u.status } : u)));
      } else {
        alert(res?.message || 'Thao tác thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi khi thay đổi trạng thái.');
    }
  };

  const handleResetPassword = async (id: number) => {
    if (!confirm('Gửi link thiết lập lại mật khẩu vào Gmail của nhân viên này?')) return;
    try {
      const res = await adminUserService.resetPassword(id);
      if (res?.success) {
        alert('Đã gửi link đổi mật khẩu thành công!');
      } else {
        alert(res?.message || 'Thao tác thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi gửi yêu cầu reset mật khẩu.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Users className="w-6 h-6 text-[#F5A623]" />
            Quản lý Nhân Sự & Tài Khoản
          </h1>
          <p className="text-sm text-[#6B7280]">Tạo mới nhân viên, phân công chi nhánh rạp và thiết lập quyền quản trị</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchInitialData}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
          <button
            onClick={handleOpenCreateModal}
            className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
          >
            <Plus className="w-5 h-5" />
            Thêm nhân viên
          </button>
        </div>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Main content table */}
      <div className="bg-white border border-black/5 rounded-2xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Đang tải danh sách nhân sự...</div>
        ) : users.length === 0 ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Không tìm thấy tài khoản nhân viên nào.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[#FAFAFA] border-b border-black/5">
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Tên đăng nhập / Email</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Vai trò</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Đơn vị công tác</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider text-right">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5">
                {users.map((u) => {
                  const staffInfo = staffAssignments[u.id];
                  // Phân biệt màu sắc role
                  const roleColors =
                    u.role === USER_ROLES.ADMIN
                      ? 'bg-red-50 text-red-700 border border-red-200'
                      : u.role === USER_ROLES.MANAGER
                      ? 'bg-amber-50 text-amber-700 border border-amber-200'
                      : 'bg-blue-50 text-blue-700 border border-blue-200';

                  return (
                    <tr key={u.id} className="hover:bg-black/[0.01] transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center font-bold text-[#22232B] border border-black/5">
                            {u.username.substring(0, 2).toUpperCase()}
                          </div>
                          <div>
                            <div className="font-bold text-[#22232B]">{u.username}</div>
                            <div className="text-xs text-[#6B7280] flex items-center gap-1">
                              <Mail className="w-3 h-3" />
                              {u.email}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-3 py-1 rounded-lg text-xs font-bold ${roleColors}`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-[#22232B]">
                        {staffInfo ? (
                          <div className="space-y-0.5">
                            <div className="font-bold text-[#22232B]">{staffInfo.branch?.name}</div>
                            <div className="text-xs text-[#6B7280] flex items-center gap-1">
                              <Briefcase className="w-3.5 h-3.5" />
                              {staffInfo.position || 'Nhân viên'} (Code: {staffInfo.staffCode})
                            </div>
                          </div>
                        ) : (
                          <span className="text-[#6B7280] italic flex items-center gap-1">
                            <Shield className="w-4 h-4 text-[#F5A623]" /> Toàn chuỗi (HQ)
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <label className="relative inline-flex items-center cursor-pointer select-none">
                          <input
                            type="checkbox"
                            checked={u.status}
                            onChange={() => handleToggleStatus(u.id)}
                            className="sr-only peer"
                          />
                          <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-500"></div>
                          <span className="ml-2 text-xs font-semibold text-[#6B7280]">
                            {u.status ? 'Hoạt động' : 'Đang khóa'}
                          </span>
                        </label>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleResetPassword(u.id)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-[#3B82F6] hover:border-[#3B82F6] transition-all bg-white"
                            title="Gửi link reset mật khẩu qua Gmail"
                          >
                            <Key className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleOpenEditModal(u)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-[#F5A623] hover:border-[#F5A623] transition-all bg-white"
                            title="Chỉnh sửa nhân viên"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(u.id)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#EF4444] hover:bg-red-500/5 hover:border-red-200 transition-all bg-white"
                            title="Xóa tài khoản"
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

      {/* Modal Slideover / Popup */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {editId !== null ? 'Chỉnh sửa thông tin nhân sự' : 'Thêm nhân viên mới'}
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
              <div className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tên đăng nhập *</label>
                    <input
                      type="text"
                      required
                      disabled={editId !== null}
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="VD: nguyenvana"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white disabled:bg-gray-100 disabled:text-gray-500 text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Email làm việc *</label>
                    <input
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="VD: vana@gmail.com"
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                    />
                  </div>

                  {editId === null && (
                    <div className="sm:col-span-2">
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Mật khẩu khởi tạo</label>
                      <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Để trống nếu đặt mật khẩu mặc định (123456)"
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                      />
                    </div>
                  )}

                  <div className="sm:col-span-2">
                    <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Vai trò hệ thống</label>
                    <select
                      value={role}
                      onChange={(e) => setRole(e.target.value as any)}
                      className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                    >
                      <option value={USER_ROLES.STAFF}>Nhân viên bán vé (STAFF)</option>
                      <option value={USER_ROLES.MANAGER}>Quản lý rạp (MANAGER)</option>
                      <option value={USER_ROLES.ADMIN}>Quản trị viên hệ thống (ADMIN)</option>
                    </select>
                  </div>
                </div>

                {/* Section Thông tin công tác nếu không phải ADMIN */}
                {role !== USER_ROLES.ADMIN && (
                  <div className="border border-black/5 rounded-2xl p-4 bg-[#FAFAFA] space-y-4 animate-in fade-in duration-200">
                    <h4 className="text-xs font-bold text-[#6B7280] uppercase tracking-wider flex items-center gap-1.5">
                      <Briefcase className="w-4 h-4 text-[#F5A623]" /> Thông tin công tác
                    </h4>

                    <div>
                      <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Chi nhánh làm việc *</label>
                      <select
                        required
                        value={branchId}
                        onChange={(e) => setBranchId(Number(e.target.value))}
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B] font-semibold"
                      >
                        {branches.map((b) => (
                          <option key={b.id} value={b.id}>
                            {b.name} ({b.city})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Mã nhân viên</label>
                        <input
                          type="text"
                          value={staffCode}
                          onChange={(e) => setStaffCode(e.target.value)}
                          placeholder="STF001"
                          className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Vị trí đảm nhiệm</label>
                        <input
                          type="text"
                          value={position}
                          onChange={(e) => setPosition(e.target.value)}
                          placeholder="VD: Trưởng ca, Nhân viên quầy vé"
                          className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/25 transition-all text-[#22232B]"
                        />
                      </div>
                    </div>
                  </div>
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
                  {submitting ? 'Đang thực thi...' : 'Xác nhận thực thi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
