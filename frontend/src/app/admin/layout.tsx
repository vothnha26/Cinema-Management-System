'use client';

import AdminSidebar from '../../components/AdminSidebar';
import AdminTopbar from '../../components/AdminTopbar';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    // Che đi layout mặc định của khách hàng bằng cách phủ kín màn hình
    <div className="fixed inset-0 z-50 bg-[#FAFAFA] flex overflow-hidden">
      {/* Sidebar cố định bên trái */}
      <AdminSidebar />

      {/* Phần nội dung chính bên phải */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar trên cùng */}
        <AdminTopbar />

        {/* Nội dung trang thay đổi */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
